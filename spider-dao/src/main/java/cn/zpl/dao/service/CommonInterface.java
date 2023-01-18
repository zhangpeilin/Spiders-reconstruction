package cn.zpl.dao.service;

public interface CommonInterface<T> {

    boolean saveOrUpdate(T entity);
}
