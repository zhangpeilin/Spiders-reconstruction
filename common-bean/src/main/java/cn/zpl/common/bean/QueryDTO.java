package cn.zpl.common.bean;

import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@Data
public class QueryDTO {

    private String title = "";
    private String tags = "";
    private String female;
    private String male;
    private String categories = "";
    private String author = "";

    /**
     * tags拼接条件，true=and，false=or
     */
    private String condition = "";
    private boolean complete = false;
    private boolean searchOnline = false;

    /**
     * 每页显示个数
     */
    @Deprecated
    private Integer numLimit = 0;
    /**
     * 当前第几页
     */
    private Integer current = 1;

    /**
     * 每页显示个数
     */
    private Integer size = 10;

    /**
     * 查询结果总页数
     */
    private Integer pages;

    /**
     * 查询结果总个数
     */
    private Integer total;

    /**
     * 星级
     */
    private Integer star;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public Integer getNumLimit() {
        return numLimit;
    }

    public void setNumLimit(Integer numLimit) {
        this.numLimit = numLimit;
    }

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        if (current == null) {
            return;
        }
        this.current = current;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    @Override
    public String toString() {
        return "QueryDTO{" +
                "title='" + title + '\'' +
                ", tags='" + tags + '\'' +
                ", condition='" + condition + '\'' +
                ", complete=" + complete +
                ", numLimit=" + numLimit +
                ", current=" + current +
                ", size=" + size +
                ", pages=" + pages +
                ", total=" + total +
                '}';
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        try {
            map = BeanUtils.describe(this);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return map;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }
}
