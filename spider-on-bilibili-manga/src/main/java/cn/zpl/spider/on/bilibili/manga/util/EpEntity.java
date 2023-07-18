/**
  * Copyright 2023 bejson.com 
  */
package cn.zpl.spider.on.bilibili.manga.util;
import lombok.Data;

import java.util.Date;

@Data
public class EpEntity {

    private String id;
    private int ord;
    private int read;
    private int pay_mode;
    private boolean is_locked;
    private int pay_gold;
    private long size;
    private String short_title;
    private boolean is_in_free;
    private String title;
    private String cover;
    private Date pub_time;
    private int comments;
    private String unlock_expire_at;
    private int unlock_type;
    private boolean allow_wait_free;
    private String progress;
    private int like_count;
    private int chapter_id;
    private int type;
    private int extra;
    private int image_count;

}