package com.bbd.saas.utils;

import java.io.Serializable;
import java.util.List;

/**
 * 分页
 * Created by luobotao on 2016/4/10.
 */
public class PageModel<T> implements Serializable{
    //结果集
    private List<T> datas;
    //每页多少条数据
    private int pageSize = 10;
    //第几页 从0开始
    private int pageNo = 0;
    //总条数
    private long totalCount = 0;
    //总页数
    private int totalPages = 0;

    public List<T> getDatas() {
        return datas;
    }

    public void setDatas(List<T> datas) {
        this.datas = datas;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalPages() {
        return (int) (totalCount % pageSize >0 ? (totalCount/pageSize+1) :(totalCount / pageSize));
    }

}
