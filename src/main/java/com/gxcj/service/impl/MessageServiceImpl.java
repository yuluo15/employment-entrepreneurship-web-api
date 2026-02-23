package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.entity.*;
import com.gxcj.entity.query.MessageQuery;
import com.gxcj.entity.vo.MessageDetailVo;
import com.gxcj.entity.vo.MessageVo;
import com.gxcj.entity.vo.UnreadCountVo;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.*;
import com.gxcj.result.PageResult;
import com.gxcj.service.MessageService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private JobDeliveryMapper jobDeliveryMapper;

    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private CompanyMapper companyMapper;

    @Override
    public PageResult<MessageVo> getMessages(MessageQuery query) {
        // 构建查询条件
        LambdaQueryWrapper<MessageEntity> wrapper = new LambdaQueryWrapper<MessageEntity>()
                .eq(MessageEntity::getReceiverId, query.getReceiverId())
                .eq(query.getType() != null, MessageEntity::getType, query.getType())
                .eq(query.getIsRead() != null, MessageEntity::getIsRead, query.getIsRead())
                .orderByDesc(MessageEntity::getCreateTime);

        // 分页查询
        Page<MessageEntity> page = messageMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                wrapper
        );

        if (page.getRecords().isEmpty()) {
            return new PageResult<>(0L, new ArrayList<>());
        }

        // 获取所有关联的投递ID
        List<String> deliveryIds = page.getRecords().stream()
                .map(MessageEntity::getRefId)
                .filter(refId -> refId != null && !refId.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        // 查询投递记录
        Map<String, JobDeliveryEntity> deliveryMap = new HashMap<>();
        if (!deliveryIds.isEmpty()) {
            List<JobDeliveryEntity> deliveries = jobDeliveryMapper.selectList(
                    new LambdaQueryWrapper<JobDeliveryEntity>()
                            .in(JobDeliveryEntity::getId, deliveryIds)
            );
            deliveryMap = deliveries.stream()
                    .collect(Collectors.toMap(JobDeliveryEntity::getId, d -> d));
        }

        // 获取所有职位ID
        List<String> jobIds = deliveryMap.values().stream()
                .map(JobDeliveryEntity::getJobId)
                .filter(jobId -> jobId != null && !jobId.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        // 查询职位信息
        Map<String, JobEntity> jobMap = new HashMap<>();
        if (!jobIds.isEmpty()) {
            List<JobEntity> jobs = jobMapper.selectList(
                    new LambdaQueryWrapper<JobEntity>()
                            .in(JobEntity::getId, jobIds)
            );
            jobMap = jobs.stream()
                    .collect(Collectors.toMap(JobEntity::getId, j -> j));
        }

        // 获取所有公司ID
        List<String> companyIds = jobMap.values().stream()
                .map(JobEntity::getCompanyId)
                .filter(companyId -> companyId != null && !companyId.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        // 查询公司信息
        Map<String, CompanyEntity> companyMap = new HashMap<>();
        if (!companyIds.isEmpty()) {
            List<CompanyEntity> companies = companyMapper.selectList(
                    new LambdaQueryWrapper<CompanyEntity>()
                            .in(CompanyEntity::getId, companyIds)
            );
            companyMap = companies.stream()
                    .collect(Collectors.toMap(CompanyEntity::getId, c -> c));
        }

        // 组装VO
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<MessageVo> voList = new ArrayList<>();
        
        final Map<String, JobDeliveryEntity> finalDeliveryMap = deliveryMap;
        final Map<String, JobEntity> finalJobMap = jobMap;
        final Map<String, CompanyEntity> finalCompanyMap = companyMap;

        for (MessageEntity message : page.getRecords()) {
            MessageVo vo = new MessageVo();
            vo.setId(message.getId());
            vo.setTitle(message.getTitle());
            vo.setContent(message.getContent());
            vo.setType(message.getType());
            vo.setIsRead(message.getIsRead());
            vo.setRefId(message.getRefId());
            vo.setCreateTime(message.getCreateTime() != null ? sdf.format(message.getCreateTime()) : null);

            // 设置公司名称和职位名称
            if (message.getRefId() != null && finalDeliveryMap.containsKey(message.getRefId())) {
                JobDeliveryEntity delivery = finalDeliveryMap.get(message.getRefId());
                if (delivery.getJobId() != null && finalJobMap.containsKey(delivery.getJobId())) {
                    JobEntity job = finalJobMap.get(delivery.getJobId());
                    vo.setPositionName(job.getJobName());
                    
                    if (job.getCompanyId() != null && finalCompanyMap.containsKey(job.getCompanyId())) {
                        CompanyEntity company = finalCompanyMap.get(job.getCompanyId());
                        vo.setCompanyName(company.getName());
                    }
                }
            }

            voList.add(vo);
        }

        return new PageResult<>(page.getTotal(), voList);
    }

    @Override
    @Transactional
    public MessageDetailVo getMessageDetail(String id, String studentId) {
        // 查询消息
        MessageEntity message = messageMapper.selectOne(
                new LambdaQueryWrapper<MessageEntity>()
                        .eq(MessageEntity::getId, id)
                        .eq(MessageEntity::getReceiverId, studentId)
        );

        if (message == null) {
            throw new BusinessException("消息不存在");
        }

        // 组装详情VO
        MessageDetailVo vo = new MessageDetailVo();
        BeanUtils.copyProperties(message, vo);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        vo.setCreateTime(message.getCreateTime() != null ? sdf.format(message.getCreateTime()) : null);

        // 查询关联信息
        if (message.getRefId() != null && !message.getRefId().isEmpty()) {
            JobDeliveryEntity delivery = jobDeliveryMapper.selectById(message.getRefId());
            if (delivery != null) {
                vo.setApplicationId(delivery.getId());
                
                if (delivery.getJobId() != null) {
                    JobEntity job = jobMapper.selectById(delivery.getJobId());
                    if (job != null) {
                        vo.setPositionId(job.getId());
                        vo.setPositionName(job.getJobName());
                        
                        if (job.getCompanyId() != null) {
                            CompanyEntity company = companyMapper.selectById(job.getCompanyId());
                            if (company != null) {
                                vo.setCompanyName(company.getName());
                            }
                        }
                    }
                }
            }
        }

        // 自动标记为已读
        if (message.getIsRead() == 0) {
            message.setIsRead(1);
            messageMapper.updateById(message);
        }

        return vo;
    }

    @Override
    public UnreadCountVo getUnreadCount(String studentId) {
        // 查询未读消息
        List<MessageEntity> unreadMessages = messageMapper.selectList(
                new LambdaQueryWrapper<MessageEntity>()
                        .eq(MessageEntity::getReceiverId, studentId)
                        .eq(MessageEntity::getIsRead, 0)
        );

        UnreadCountVo vo = new UnreadCountVo();
        vo.setTotal(unreadMessages.size());
        vo.setSystemCount((int) unreadMessages.stream().filter(m -> m.getType() == 1).count());
        vo.setInterviewCount((int) unreadMessages.stream().filter(m -> m.getType() == 2).count());
        vo.setOfferCount((int) unreadMessages.stream().filter(m -> m.getType() == 3).count());

        return vo;
    }

    @Override
    public int markAsRead(String id, String studentId) {
        MessageEntity message = new MessageEntity();
        message.setId(id);
        message.setIsRead(1);

        return messageMapper.update(message,
                new LambdaQueryWrapper<MessageEntity>()
                        .eq(MessageEntity::getId, id)
                        .eq(MessageEntity::getReceiverId, studentId)
                        .eq(MessageEntity::getIsRead, 0)
        );
    }

    @Override
    public int markAllAsRead(String studentId, Integer type) {
        MessageEntity message = new MessageEntity();
        message.setIsRead(1);

        LambdaQueryWrapper<MessageEntity> wrapper = new LambdaQueryWrapper<MessageEntity>()
                .eq(MessageEntity::getReceiverId, studentId)
                .eq(MessageEntity::getIsRead, 0);

        if (type != null) {
            wrapper.eq(MessageEntity::getType, type);
        }

        return messageMapper.update(message, wrapper);
    }

    @Override
    public int deleteMessage(String id, String studentId) {
        return messageMapper.delete(
                new LambdaQueryWrapper<MessageEntity>()
                        .eq(MessageEntity::getId, id)
                        .eq(MessageEntity::getReceiverId, studentId)
        );
    }
}
