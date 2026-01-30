package com.gxcj.result;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private Long total;
    private List<T> data;

    public PageResult(){}

    public PageResult(Long total, List<T> data){
        this.total = total;
        this.data = data;
    }

    public static <E1,E2> PageResult<E2> of(List<E1> list,Wrapper<E1,E2> wrapper){
        PageResult<E2> result = new PageResult<>();
        result.setData(list.stream().map(wrapper::wrap).toList());
        return  result;
    }

    public interface Wrapper<E1,E2>{
        E2 wrap(E1 src);
    }
}
