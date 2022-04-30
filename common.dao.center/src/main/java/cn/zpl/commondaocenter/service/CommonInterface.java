package cn.zpl.commondaocenter.service;

public interface CommonInterface<T> {

    boolean saveOrUpdate(T entity);
}
