package com.tds.datar.common.core;

import java.util.List;

/**
 * 分页结果封装
 */
public class PageResult<T> {

    private List<T> list;
    private Pagination pagination;

    public PageResult() {}

    public PageResult(List<T> list, Pagination pagination) {
        this.list = list;
        this.pagination = pagination;
    }

    public static <T> PageResult<T> of(List<T> list, long total, int currentPage, int pageSize) {
        Pagination pagination = new Pagination();
        pagination.setTotal(total);
        pagination.setCurrentPage(currentPage);
        pagination.setPageSize(pageSize);
        pagination.setTotalPages((int) Math.ceil((double) total / pageSize));
        return new PageResult<>(list, pagination);
    }

    public static <T> PageResult<T> of(List<T> list, long total) {
        Pagination pagination = new Pagination();
        pagination.setTotal(total);
        return new PageResult<>(list, pagination);
    }

    public static class Pagination {
        private long total;
        private int currentPage;
        private int pageSize;
        private int totalPages;

        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        public int getCurrentPage() { return currentPage; }
        public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    }

    public List<T> getList() { return list; }
    public void setList(List<T> list) { this.list = list; }
    public Pagination getPagination() { return pagination; }
    public void setPagination(Pagination pagination) { this.pagination = pagination; }
}