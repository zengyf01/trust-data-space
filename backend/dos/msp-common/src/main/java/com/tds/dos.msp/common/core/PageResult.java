package com.tds.dos.msp.common.core;

import java.util.List;

/**
 * Unified pagination result
 */
public class PageResult<T> {
    private List<T> list;
    private Pagination pagination;

    public PageResult() {
    }

    public PageResult(List<T> list, Pagination pagination) {
        this.list = list;
        this.pagination = pagination;
    }

    public static <T> PageResult<T> of(List<T> list, long total, int currentPage, int pageSize) {
        Pagination pagination = new Pagination(total, currentPage, pageSize);
        return new PageResult<>(list, pagination);
    }

    public List<T> getList() { return list; }
    public void setList(List<T> list) { this.list = list; }
    public Pagination getPagination() { return pagination; }
    public void setPagination(Pagination pagination) { this.pagination = pagination; }

    public static class Pagination {
        private long total;
        private int currentPage;
        private int pageSize;

        public Pagination() {
        }

        public Pagination(long total, int currentPage, int pageSize) {
            this.total = total;
            this.currentPage = currentPage;
            this.pageSize = pageSize;
        }

        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        public int getCurrentPage() { return currentPage; }
        public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    }
}