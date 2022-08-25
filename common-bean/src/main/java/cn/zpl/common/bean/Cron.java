package cn.zpl.common.bean;

import io.swagger.annotations.ApiModel;

import java.io.Serializable;

/**
     * <p>
     *
     * </p>
     *
     * @author zpl
     * @since 2022-08-25
     */
    @ApiModel(value = "Cron对象", description = "")
    public class Cron implements Serializable {

        private static final long serialVersionUID = 1L;

        private String cronId;

        private String cron;

        public String getCronId() {
            return cronId;
        }

        public void setCronId(String cronId) {
            this.cronId = cronId;
        }
        public String getCron() {
            return cron;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }

        @Override
        public String toString() {
            return "Cron{" +
                "cronId=" + cronId +
                ", cron=" + cron +
            "}";
        }
    }