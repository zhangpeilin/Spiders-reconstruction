package cn.zpl.common.bean;


import cn.zpl.common.bean.Bika;

import java.util.List;

public class RestResponseBean {

    private int error;
    private List<Bika> list;
    private int total;
    private int pages;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public void setError(int error) {
        this.error = error;
    }

    public int getError() {
        return error;
    }

    public void setList(List<Bika> list) {
        this.list = list;
    }

    public List<Bika> getList() {
        return list;
    }

    public boolean isSuccess() {
        return error == 0;
    }
}