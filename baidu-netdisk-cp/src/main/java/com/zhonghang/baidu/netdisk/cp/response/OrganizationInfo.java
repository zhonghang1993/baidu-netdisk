package com.zhonghang.baidu.netdisk.cp.response;

import lombok.Data;

/**
 * Created by zhonghang  2022/1/4.
 */
@Data
public class OrganizationInfo {
    private Long cid; //企业空间id
    private OrgInfo orgInfo;
    private Long role; //角色：0-普通用户 1-超级管理员 2-子管理员

    @Data
    public static class OrgInfo{
        private String adminavatar;
        private String adminname;
        private String adminnamein;
        private Long adminuk ;
        private String brief ;
        private Long cert_etime ;
        private Long cert_status ;
        private Long cert_stime ;
        private Long cid ;
        private Long company_quota ;
        private Long company_quota_used ;
        private Long current_user_num ;
        private String logo ;
        private String name ;
        private Integer product_endtime ;
        private String product_name ;
        private Long product_starttime ;
        private Long product_status ;
        private Long product_type ;
        private Long role ;
        private Long slspeeduptotalcount ;
        private Long slspeedupusedcount ;
        private Long total_user_num ;
        private Long type ;
        private Long uk ; //用户身份id
    }
}
