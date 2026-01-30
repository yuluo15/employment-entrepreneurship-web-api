package com.gxcj.controller.admin;

import com.gxcj.entity.DictDataEntity;
import com.gxcj.entity.DictTypeEntity;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.DictService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dictMgr")
public class DictController {

    @Autowired
    private DictService dictService;

    @GetMapping("/listType")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<PageResult<DictTypeEntity>> listType(@RequestParam Integer pageNum, @RequestParam Integer pageSize, @RequestParam String dictName) {
        PageResult<DictTypeEntity> pageResult = dictService.listType(pageNum, pageSize, dictName);
        return Result.success(pageResult);
    }

    @PostMapping("/creType")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<String> createType(@RequestBody @Valid CreTypeReq req){
        dictService.createType(req);
        return Result.success();
    }

    @PostMapping("/updType")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<String> updateType(@RequestBody @Valid CreTypeReq req){
        dictService.updateType(req);
        return Result.success();
    }

    @DeleteMapping("/delType/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<String> deleteType(@PathVariable String id){
        dictService.deleteType(id);
        return Result.success();
    }

    @GetMapping("/list/data")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<PageResult<DictDataEntity>> listData(@RequestParam Integer pageNum, Integer pageSize, @RequestParam String dictType) {
        PageResult<DictDataEntity> pageResult = dictService.listData(pageNum, pageSize, dictType);
        return Result.success(pageResult);
    }

    @PostMapping("/creData")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<String> createData(@RequestBody @Valid CreDataReq req){
        dictService.createDate(req);
        return Result.success();
    }

    @PostMapping("/updData")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<String> updateData(@RequestBody @Valid CreDataReq req){
        dictService.updateData(req);
        return Result.success();
    }

    @DeleteMapping("/delData/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<String> deleteData(@PathVariable String id){
        dictService.deleteData(id);
        return Result.success();
    }

    @Data
    public static class CreTypeReq{
        //创建时不需要传，更新时需要传
        private String id;
        @NotBlank
        private String dictName;
        @NotBlank
        private String dictType;
        private Integer status;
        private String remark;
    }

    @Data
    public static class CreDataReq{
        //创建时不需要传，更新时需要传
        private String id;
        private Integer dictSort;
        private String dictLabel;
        private String dictValue;
        @NotBlank
        private String dictType;
        private Integer status;
        private String remark;
    }

}
