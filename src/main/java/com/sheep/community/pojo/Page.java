package com.sheep.community.pojo;

/**
 * 用于封装分页相关的信息
 *
 * @author sheep
 */
public class Page {
    //当前页码
    private Integer current = 1;
    //页面上限行数
    private Integer limit = 10;
    //数据总数
    private Integer rows;
    //查询路径
    private String path;

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        if (current > 0) {
            this.current = current;
        }
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        if (rows >= 0) {
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页的起始行
     */
    public Integer getOffset() {
        return (current - 1) * limit;
    }

    /**
     * 获取总页数
     */
    public Integer getTotal() {
        return (rows + limit - 1) / limit;
    }

    /**
     * 获取起始页码
     */
    public Integer getFrom() {
        int from = current - 2;
        return Math.max(from, 1);
    }

    /**
     * 获取结束页码
     */
    public Integer getTo() {
        int to = Math.max(current + 2, 5);
        return Math.min(to, getTotal());
    }
}
