package com.maths22.ftcmanuals.resources;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.AbstractList;
import java.util.ArrayList;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class Page<T> extends AbstractList<T> {
    @JsonProperty("items")
    private ArrayList<T> list;

    @JsonProperty
    private long totalSize;
    @JsonProperty
    private long totalPageCount;
    @JsonProperty
    private int pageNumber;

    public Page() {
        list = new ArrayList<>();
    }

    @JsonProperty
    @Override
    public int size() {
        return list.size();
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public T set(int index, T element) {
        return list.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        list.add(index, element);
    }

    @Override
    public T remove(int index) {
        return list.remove(index);
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getTotalPageCount() {
        return totalPageCount;
    }

    public void setTotalPageCount(long totalPageCount) {
        this.totalPageCount = totalPageCount;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }
}
