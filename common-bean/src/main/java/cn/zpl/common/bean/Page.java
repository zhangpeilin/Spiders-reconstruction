package cn.zpl.common.bean;

import lombok.Data;

@Data
public class Page {
    private long current;
    private long size;

    public Page(long current, long size) {
        this.current = current;
        this.size = size;
    }

    @Override
    public String toString() {
        return current + "," + size;
    }
}
