
-- ----------------------------
-- 存储过程
-- ----------------------------
-- 新客回款情况
DROP PROCEDURE IF EXISTS `NewUserRepayRate`;
CREATE PROCEDURE `NewUserRepayRate`(IN merch varchar(20),IN userSource varchar(20),IN userOrigin varchar(20),IN startTime varchar(20),IN endTime varchar(20))
BEGIN
DECLARE var_sql varchar(10000);
set var_sql = '
select
DATE_FORMAT(give_time,''%Y%m%d'') as give_time,
DATE_FORMAT(DATE_ADD(give_time,INTERVAL +5 DAY),''%Y%m%d'') AS nature_give_time,
CASE shmc
    WHEN ''mx'' THEN ''点滴''
    WHEN ''dawang'' THEN ''大王''
    WHEN ''huijie'' THEN ''惠借''
    WHEN ''haitun'' THEN ''海豚''
    WHEN ''fruit'' THEN ''火龙果''
    WHEN ''white'' THEN ''白龙马''
    WHEN ''have'' THEN ''全橙有借''
    WHEN ''parva'' THEN ''小鲸鱼''
    WHEN ''farm'' THEN ''惠农花''
    WHEN ''quick'' THEN ''快花花''
    WHEN ''lai'' THEN ''快来贷''
    WHEN ''time'' THEN ''时时钱包''
    WHEN ''fly'' THEN ''大象优借''
    WHEN ''net'' THEN ''网银分期''
    WHEN ''smile'' THEN ''妃子笑''
    WHEN ''loan'' THEN ''贷贷有财''
    WHEN ''opt'' THEN ''速亿通''
    WHEN ''fine'' THEN ''亿得金''
    WHEN ''bent'' THEN ''趣哪花''
    WHEN ''dit'' THEN ''信达钱包''
    WHEN ''spot'' THEN ''点来米''
    WHEN ''baby'' THEN ''宝宝钱包''
    WHEN ''come'' THEN ''马来钱包''
    WHEN ''oxen'' THEN ''小黄牛''
    WHEN ''rice'' THEN ''小灰机''
    WHEN ''mouse'' THEN ''牛牛袋子''
    WHEN ''big'' THEN ''大众钱袋''
    WHEN ''bloo'' THEN ''钱亿宝''
    WHEN ''free'' THEN ''有借无忧''
    WHEN ''vite'' THEN ''V速''
    WHEN ''beer'' THEN ''啤酒花''
    WHEN ''moon'' THEN ''九月花''
    WHEN ''sth'' THEN ''小泡芙''
    ELSE ''未知''
END AS merchant_name,
count(DISTINCT case WHEN give_time > 0 THEN  order_id ELSE null END) AS pay_count,
count(DISTINCT case WHEN user_type = 1 AND give_time > 0 THEN order_id ELSE null END) AS new_user_count,
count(DISTINCT case WHEN user_type = 2 AND give_time > 0 THEN order_id ELSE null END) AS two_user_count,
count(DISTINCT case WHEN user_type = 3 AND give_time > 0 THEN order_id ELSE null END) AS old_user_count,
concat(ROUND((count(DISTINCT case WHEN user_type = 1 AND give_time > 0 THEN order_id ELSE null END)
                                -
                                count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (41) THEN order_id ELSE null END )
                                -
                                count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (35) AND zq_pay_status = 3 THEN order_id ELSE null END )
                            )
                                /
                                count(DISTINCT case WHEN user_type = 1 AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),''%'') time_new_over_rate,

concat(ROUND(
                                count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (33,34,37,38) THEN order_id ELSE null END )
                                /
                                count(DISTINCT case WHEN user_type = 1 AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),''%'') AS new_over_now_rate,

concat(ROUND(
                                count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (41,42,43) THEN order_id ELSE null END )
                                /
                                count(DISTINCT case WHEN user_type = 1 AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),''%'')  AS new_principal_pay_rate,

concat(ROUND(
                                count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (35,36) THEN order_id ELSE null END )
                                /
                                count(DISTINCT case WHEN user_type = 1 AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),''%'')  AS new_defer_rate,
count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (41) AND  (DATE_FORMAT(real_repay_time,''%Y%m%d'') <= ahead_pay_day_1 )  THEN order_id ELSE null END )  AS new_ahead_repay,
count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (41) AND  (DATE_FORMAT(real_repay_time,''%Y%m%d'') > ahead_pay_day_1 )  THEN order_id ELSE null END )  AS new_normal_repay,
count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (42) THEN order_id ELSE null END )  AS new_over_repay,
count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (33,34) THEN order_id ELSE null END )  AS new_over,
count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (35) AND zq_pay_status = 3 AND (pay_time <= old_should_pay_time )  THEN order_id ELSE null END )  AS new_ahead_defer,
count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (35) AND zq_pay_status = 3 AND (pay_time > old_should_pay_time )  THEN order_id ELSE null END )  AS new_nature_defer,
count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (36) THEN order_id ELSE null END )  AS new_over_defer,
count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (37,38) THEN order_id ELSE null END )  AS new_defer_over,
count(DISTINCT case when user_type = 1 AND give_time > 0 AND zq_pay_status = 3 THEN zq_id ELSE null END )  AS new_total_defer_time
 from (
SELECT
    o.id AS order_id,
    o.order_no,
    o.uid,
    o.borrow_day,
    o.borrow_money,
    o.actual_money,
    o.interest_rate,
    o.interest_fee,
    o.overdue_rate,
    o.overdue_day,
    o.overdue_fee,
    o.should_repay,
    o.had_repay,
    o.reduce_money,
    o.status AS order_status,
    o.create_time AS order_ctime,
    o.audit_time,
    o.arrive_time AS give_time,
    o.repay_time,
    o.real_repay_time,
    o.merchant AS shmc,
    o.user_type,
    DATE_FORMAT(
        DATE_ADD(o.repay_time, INTERVAL - 1 DAY),
        ''%Y%m%d''
    ) AS ahead_pay_day_1,
    DATE_FORMAT(
        DATE_ADD(o.repay_time, INTERVAL + 1 DAY),
        ''%Y%m%d''
    ) AS behind_pay_day_1,
    u.id As user_id,u.user_phone,u.user_origin,u.create_time as user_ctime,
    d.id as zq_id,d.pay_status AS zq_pay_status,d.pay_time,
    d.create_time AS zq_create_time,d.repay_date AS old_should_pay_time,d.defer_repay_date
FROM
    tb_order o
left join tb_user u
on o.uid = u.id
left join tb_order_audit oa on o.id=oa.order_id
left join tb_order_defer d on o.id = d.order_id
where
u.user_phone NOT IN (''15757127746'',''18557530599'',''18838515567'')
and u.merchant = ''';
set var_sql = CONCAT(var_sql,merch,'''');
if userSource <> '' and userSource is not null then
 set var_sql = concat(var_sql,' AND o.uid IN (SELECT id FROM tb_user WHERE user_nick IN (''',userSource,'''))');
end if;
if userOrigin <> '' and userOrigin is not null then
set var_sql = concat(var_sql,' AND user_origin IN (',userOrigin,')');
end if;
## 只查看当前到期的
if (startTime is null or startTime = '') and (endTime is null or endTime = '') THEN
set var_sql = concat(var_sql,' AND DATE_FORMAT(DATE_ADD(o.arrive_time,INTERVAL +5 DAY),''%Y%m%d'') < NOW()');
ELSE
 if startTime is not null and startTime <> '' then
 set var_sql = concat(var_sql,' AND o.arrive_time > ''',startTime,' 00:00:00''');
end if;
if endTime is not null and endTime <> '' then
set var_sql = concat(var_sql,' AND o.arrive_time < ''',endTime,' 00:00:00''');
end if;
end if;

set var_sql = concat(var_sql,') temp GROUP BY 1,3 ORDER BY 3,1 DESC');
set @sql = var_sql;
PREPARE stmt from @sql;
EXECUTE stmt;
END

-- 新客回款情况详情
DROP PROCEDURE IF EXISTS `NewUserRepayRateDetail`;
CREATE PROCEDURE `NewUserRepayRateDetail`(IN merch varchar(20),IN userSource varchar(20),IN userOrigin varchar(20),IN dateStr varchar(20))
BEGIN
DECLARE var_sql varchar(10000);
set var_sql = '
select
origin_name,
CASE shmc
    WHEN ''mx'' THEN ''点滴''
    WHEN ''dawang'' THEN ''大王''
    WHEN ''huijie'' THEN ''惠借''
    WHEN ''haitun'' THEN ''海豚''
    WHEN ''fruit'' THEN ''火龙果''
    WHEN ''white'' THEN ''白龙马''
    WHEN ''have'' THEN ''全橙有借''
    WHEN ''parva'' THEN ''小鲸鱼''
    WHEN ''farm'' THEN ''惠农花''
    WHEN ''quick'' THEN ''快花花''
    WHEN ''lai'' THEN ''快来贷''
    WHEN ''time'' THEN ''时时钱包''
    WHEN ''fly'' THEN ''大象优借''
    WHEN ''net'' THEN ''网银分期''
    WHEN ''smile'' THEN ''妃子笑''
    WHEN ''loan'' THEN ''贷贷有财''
    WHEN ''opt'' THEN ''速亿通''
    WHEN ''fine'' THEN ''亿得金''
    WHEN ''bent'' THEN ''趣哪花''
    WHEN ''dit'' THEN ''信达钱包''
    WHEN ''spot'' THEN ''点来米''
    WHEN ''baby'' THEN ''宝宝钱包''
    WHEN ''come'' THEN ''马来钱包''
    WHEN ''oxen'' THEN ''小黄牛''
    WHEN ''rice'' THEN ''小灰机''
    WHEN ''mouse'' THEN ''牛牛袋子''
    WHEN ''big'' THEN ''大众钱袋''
    WHEN ''bloo'' THEN ''钱亿宝''
    WHEN ''free'' THEN ''有借无忧''
    WHEN ''vite'' THEN ''V速''
    WHEN ''beer'' THEN ''啤酒花''
    WHEN ''moon'' THEN ''九月花''
    WHEN ''sth'' THEN ''小泡芙''
    ELSE ''未知''
END AS merchant_name,
count(DISTINCT case WHEN give_time > 0 THEN  order_id ELSE null END) AS pay_count,
count(DISTINCT case WHEN user_type = 1 AND give_time > 0 THEN order_id ELSE null END) AS new_user_count,
count(DISTINCT case WHEN user_type = 2 AND give_time > 0 THEN order_id ELSE null END) AS two_user_count,
count(DISTINCT case WHEN user_type = 3 AND give_time > 0 THEN order_id ELSE null END) AS old_user_count,
concat(ROUND((count(DISTINCT case WHEN user_type = 1 AND give_time > 0 THEN order_id ELSE null END)
                                -
                                count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (41) THEN order_id ELSE null END )
                                -
                                count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (35,37) AND zq_pay_status = 3 THEN order_id ELSE null END )
                            )
                                /
                                count(DISTINCT case WHEN user_type = 1 AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),''%'') time_new_over_rate,

concat(ROUND(
                                count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (33,34,37,38) THEN order_id ELSE null END )
                                /
                                count(DISTINCT case WHEN user_type = 1 AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),''%'') AS new_over_now_rate,

concat(ROUND(
                                count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (41,42,43) THEN order_id ELSE null END )
                                /
                                count(DISTINCT case WHEN user_type = 1 AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),''%'')  AS new_principal_pay_rate,

concat(ROUND(
                                count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (35,36) THEN order_id ELSE null END )
                                /
                                count(DISTINCT case WHEN user_type = 1 AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),''%'')  AS new_defer_rate,
count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (41) AND  (DATE_FORMAT(real_repay_time,''%Y%m%d'') <= ahead_pay_day_1 )  THEN order_id ELSE null END )  AS new_ahead_repay,
count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (41) AND  (DATE_FORMAT(real_repay_time,''%Y%m%d'') > ahead_pay_day_1 )  THEN order_id ELSE null END )  AS new_normal_repay,
count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (42) THEN order_id ELSE null END )  AS new_over_repay,
count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (33,34) THEN order_id ELSE null END )  AS new_over,
count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (35) AND zq_pay_status = 3 AND (pay_time <= old_should_pay_time )  THEN order_id ELSE null END )  AS new_ahead_defer,
count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (35) AND zq_pay_status = 3 AND (pay_time > old_should_pay_time )  THEN order_id ELSE null END )  AS new_nature_defer,
count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (36) THEN order_id ELSE null END )  AS new_over_defer,
count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (37,38) THEN order_id ELSE null END )  AS new_defer_over,
count(DISTINCT case when user_type = 1 AND give_time > 0 AND zq_pay_status = 3 THEN zq_id ELSE null END )  AS new_total_defer_time
 from (
SELECT
    o.id AS order_id,
    o.order_no,
    o.uid,
    o.borrow_day,
    o.borrow_money,
    o.actual_money,
    o.interest_rate,
    o.interest_fee,
    o.overdue_rate,
    o.overdue_day,
    o.overdue_fee,
    o.should_repay,
    o.had_repay,
    o.reduce_money,
    o.status AS order_status,
    o.create_time AS order_ctime,
    o.audit_time,
    o.arrive_time AS give_time,
    o.repay_time,
    o.real_repay_time,
    o.merchant AS shmc,
    o.user_type,
		m.origin_name,
    DATE_FORMAT(
        DATE_ADD(o.repay_time, INTERVAL - 1 DAY),
        ''%Y%m%d''
    ) AS ahead_pay_day_1,
    DATE_FORMAT(
        DATE_ADD(o.repay_time, INTERVAL + 1 DAY),
        ''%Y%m%d''
    ) AS behind_pay_day_1,
    u.id As user_id,u.user_phone,u.user_origin,u.create_time as user_ctime,
    d.id as zq_id,d.pay_status AS zq_pay_status,d.pay_time,
    d.create_time AS zq_create_time,d.repay_date AS old_should_pay_time,d.defer_repay_date
FROM
    tb_order o
left join tb_user u on o.uid = u.id
left join tb_order_audit oa on o.id=oa.order_id
left join tb_order_defer d on o.id = d.order_id
left join tb_merchant_origin m on m.id = u.user_origin
where
u.user_phone NOT IN (''15757127746'',''18557530599'',''18838515567'')
and u.merchant = ''';
set var_sql = CONCAT(var_sql,merch,'''');
set var_sql = CONCAT(var_sql,' AND DATE_FORMAT(DATE_ADD(o.arrive_time,INTERVAL +5 DAY),''%Y%m%d'') < NOW()
AND DATE_FORMAT(arrive_time,''%Y%m%d'') = ',dateStr,'');

if userSource <> '' and userSource is not null then
 set var_sql = concat(var_sql,' AND o.uid IN (SELECT id FROM tb_user WHERE user_nick IN (''',userSource,'''))');
end if;
if userOrigin <> '' and userOrigin is not null then
set var_sql = concat(var_sql,' AND user_origin IN (',userOrigin,')');
end if;

set var_sql = concat(var_sql,') temp GROUP BY origin_name');
set @sql = var_sql;
PREPARE stmt from @sql;
EXECUTE stmt;
END

-- 老客回款情况
DROP PROCEDURE IF EXISTS `OldUserRepayRate`;
CREATE PROCEDURE `OldUserRepayRate`(IN merch varchar(20),IN userSource varchar(20),IN userOrigin varchar(20),IN startTime varchar(20),IN endTime varchar(20))
BEGIN
DECLARE var_sql varchar(10000);
set var_sql = '
SELECT DATE_FORMAT(give_time,''%Y%m%d'') as give_time,DATE_FORMAT(DATE_ADD(give_time,INTERVAL +5 DAY),''%Y%m%d'') AS nature_give_time,borrow_money,
count(DISTINCT case WHEN user_type = 3 AND give_time > 0 THEN order_id ELSE null END) AS old_user_count,
concat(ROUND(
                            (
                                count(DISTINCT case WHEN user_type = 3 AND give_time > 0 THEN order_id ELSE null END)
                                -
                                count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (41) THEN order_id ELSE NULL END )
                                -
                                count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (35,37) AND zq_pay_status = 3 THEN order_id ELSE NULL END )
                            )
                                /
                                count(DISTINCT case WHEN user_type = 3 AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),''%'') time_old_over_rate,

concat(ROUND(
                                count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (33,34,37,38) THEN order_id ELSE NULL END )
                                /
                                count(DISTINCT case WHEN user_type = 3 AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),''%'') AS old_over_now_rate,

concat(ROUND(
                                count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (41,42,43) THEN order_id ELSE NULL END )
                                /
                                count(DISTINCT case WHEN user_type = 3 AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),''%'')  AS old_principal_pay_rate,

concat(ROUND(
                                count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (35,36) THEN order_id ELSE NULL END )
                                /
                                count(DISTINCT case WHEN user_type = 3 AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),''%'')  AS old_defer_rate,
count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (41) AND  (DATE_FORMAT(real_repay_time,''%Y%m%d'') <= ahead_pay_day_1 )  THEN order_id ELSE NULL END )  AS old_ahead_repay,
count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (41) AND  (DATE_FORMAT(real_repay_time,''%Y%m%d'') > ahead_pay_day_1 )  THEN order_id ELSE NULL END )  AS old_normal_repay,
count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (42) THEN order_id ELSE NULL END )  AS old_over_repay,
count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (33,34) THEN order_id ELSE NULL END )  AS old_over,
count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (35) AND (pay_time <= old_should_pay_time )  THEN order_id ELSE NULL END )  AS old_ahead_defer,
count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (35) AND (pay_time > old_should_pay_time )  THEN order_id ELSE NULL END )  AS old_nature_defer,
count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (36) THEN order_id ELSE NULL END )  AS old_over_defer,
count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (37,38) THEN order_id ELSE NULL END )  AS old_defer_over,
count(DISTINCT case when user_type = 3 AND give_time > 0 AND zq_pay_status = 3 THEN zq_id ELSE NULL END )  AS old_total_defer_time
FROM
(
SELECT
*
FROM
(
SELECT
id AS order_id,order_no,uid,borrow_day,borrow_money,actual_money,interest_rate,interest_fee,overdue_rate,overdue_day,overdue_fee,should_repay,had_repay,reduce_money,`status` AS order_status,create_time AS order_ctime,audit_time,arrive_time AS give_time,repay_time,real_repay_time,merchant,user_type,
DATE_FORMAT(DATE_ADD(repay_time,INTERVAL -1 DAY),''%Y%m%d'') AS ahead_pay_day_1,
DATE_FORMAT(DATE_ADD(repay_time,INTERVAL +1 DAY),''%Y%m%d'') AS behind_pay_day_1
FROM `tb_order`
)AS aa
LEFT JOIN
(
SELECT
id AS user_id,user_name,user_phone,user_cert_no,user_origin,create_time AS user_ctime
FROM
tb_user
)AS bb
ON aa.uid = bb.user_id
LEFT JOIN
(
SELECT
order_id AS rg_order_id,audit_id,audit_name,fail_reason,`status` AS rg_status,crete_time AS rg_ctime
FROM
tb_order_audit
)AS cc
ON aa.order_id = cc.rg_order_id
LEFT JOIN
(
SELECT
id AS zq_id,order_id AS zq_order_id,defer_day,defer_fee,defer_total_fee,defer_times,pay_type AS zq_pay_type,pay_no AS zq_pay_no,pay_status AS zq_pay_status,pay_time,create_time AS zq_create_time,repay_date AS old_should_pay_time,defer_repay_date
FROM
tb_order_defer
WHERE pay_status = 3
)AS dd
ON aa.order_id = dd.zq_order_id
WHERE user_type =3
AND user_phone NOT IN (''15757127746'',''18557530599'',''18838515567'')
)AS abc
WHERE merchant = ''';
set var_sql = CONCAT(var_sql,merch,'''');
if userSource <> '' and userSource is not null then
 set var_sql = concat(var_sql,' AND uid IN (SELECT id FROM tb_user WHERE user_nick IN (''',userSource,'''))');
end if;
if userOrigin <> '' and userOrigin is not null then
set var_sql = concat(var_sql,' AND user_origin IN (',userOrigin,')');
end if;
## 只查看当前到期的
if (startTime is null or startTime = '') and (endTime is null or endTime = '') THEN
set var_sql = concat(var_sql,' AND DATE_FORMAT(DATE_ADD(give_time,INTERVAL +5 DAY),''%Y%m%d'') < NOW()');
ELSE
 if startTime is not null and startTime <> '' then
 set var_sql = concat(var_sql,' AND give_time > ''',startTime,' 00:00:00''');
end if;
if endTime is not null and endTime <> '' then
set var_sql = concat(var_sql,' AND give_time < ''',endTime,' 00:00:00''');
end if;
end if;
set var_sql = concat(var_sql,' GROUP BY 1,3 ORDER BY 1 DESC');
set @sql = var_sql;
PREPARE stmt from @sql;
EXECUTE stmt;
END

-- 老客回款情况详情
DROP PROCEDURE IF EXISTS `OldUserRepayRateDetail`;
CREATE PROCEDURE `OldUserRepayRateDetail`(IN merch varchar(20),IN userSource varchar(20),IN userOrigin varchar(20),IN dateStr varchar(20))
BEGIN
DECLARE var_sql varchar(10000);
set var_sql = '
SELECT origin_name,borrow_money,
count(DISTINCT case WHEN user_type = 3 AND give_time > 0 THEN order_id ELSE null END) AS old_user_count,
concat(ROUND(
                            (
                                count(DISTINCT case WHEN user_type = 3 AND give_time > 0 THEN order_id ELSE null END)
                                -
                                count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (41) THEN order_id ELSE NULL END )
                                -
                                count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (35,37) AND zq_pay_status = 3 THEN order_id ELSE NULL END )
                            )
                                /
                                count(DISTINCT case WHEN user_type = 3 AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),''%'') time_old_over_rate,

concat(ROUND(
                                count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (33,34,37,38) THEN order_id ELSE NULL END )
                                /
                                count(DISTINCT case WHEN user_type = 3 AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),''%'') AS old_over_now_rate,

concat(ROUND(
                                count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (41,42,43) THEN order_id ELSE NULL END )
                                /
                                count(DISTINCT case WHEN user_type = 3 AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),''%'')  AS old_principal_pay_rate,

concat(ROUND(
                                count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (35,36) THEN order_id ELSE NULL END )
                                /
                                count(DISTINCT case WHEN user_type = 3 AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),''%'')  AS old_defer_rate,
count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (41) AND  (DATE_FORMAT(real_repay_time,''%Y%m%d'') <= ahead_pay_day_1 )  THEN order_id ELSE NULL END )  AS old_ahead_repay,
count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (41) AND  (DATE_FORMAT(real_repay_time,''%Y%m%d'') > ahead_pay_day_1 )  THEN order_id ELSE NULL END )  AS old_normal_repay,
count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (42) THEN order_id ELSE NULL END )  AS old_over_repay,
count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (33,34) THEN order_id ELSE NULL END )  AS old_over,
count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (35) AND (pay_time <= old_should_pay_time )  THEN order_id ELSE NULL END )  AS old_ahead_defer,
count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (35) AND (pay_time > old_should_pay_time )  THEN order_id ELSE NULL END )  AS old_nature_defer,
count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (36) THEN order_id ELSE NULL END )  AS old_over_defer,
count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (37,38) THEN order_id ELSE NULL END )  AS old_defer_over,
count(DISTINCT case when user_type = 3 AND give_time > 0 AND zq_pay_status = 3 THEN zq_id ELSE NULL END )  AS old_total_defer_time
FROM
(
SELECT
*
FROM
(
SELECT
id AS order_id,order_no,uid,borrow_day,borrow_money,actual_money,interest_rate,interest_fee,overdue_rate,overdue_day,overdue_fee,should_repay,had_repay,reduce_money,`status` AS order_status,create_time AS order_ctime,audit_time,arrive_time AS give_time,repay_time,real_repay_time,merchant,user_type,
DATE_FORMAT(DATE_ADD(repay_time,INTERVAL -1 DAY),''%Y%m%d'') AS ahead_pay_day_1,
DATE_FORMAT(DATE_ADD(repay_time,INTERVAL +1 DAY),''%Y%m%d'') AS behind_pay_day_1
FROM `tb_order`
)AS aa
LEFT JOIN
(
SELECT
u.id AS user_id,u.user_name,u.user_phone,u.user_cert_no,u.user_origin,m.origin_name,u.create_time AS user_ctime
FROM
tb_user u
left join tb_merchant_origin m on m.id = u.user_origin
)AS bb
ON aa.uid = bb.user_id
LEFT JOIN
(
SELECT
order_id AS rg_order_id,audit_id,audit_name,fail_reason,`status` AS rg_status,crete_time AS rg_ctime
FROM
tb_order_audit
)AS cc
ON aa.order_id = cc.rg_order_id
LEFT JOIN
(
SELECT
id AS zq_id,order_id AS zq_order_id,defer_day,defer_fee,defer_total_fee,defer_times,pay_type AS zq_pay_type,pay_no AS zq_pay_no,pay_status AS zq_pay_status,pay_time,create_time AS zq_create_time,repay_date AS old_should_pay_time,defer_repay_date
FROM
tb_order_defer
WHERE pay_status = 3
)AS dd
ON aa.order_id = dd.zq_order_id
WHERE user_type =3
AND user_phone NOT IN (''15757127746'',''18557530599'',''18838515567'')
)AS abc

WHERE merchant = ''';

set var_sql = CONCAT(var_sql,merch,'''');
set var_sql = CONCAT(var_sql,' AND DATE_FORMAT(DATE_ADD(give_time,INTERVAL +5 DAY),''%Y%m%d'') < NOW()
AND DATE_FORMAT(give_time,''%Y%m%d'') = ',dateStr,'');

if userSource <> '' and userSource is not null then
 set var_sql = concat(var_sql,' AND uid IN (SELECT id FROM tb_user WHERE user_nick IN (''',userSource,'''))');
end if;
if userOrigin <> '' and userOrigin is not null then
set var_sql = concat(var_sql,' AND user_origin IN (',userOrigin,')');
end if;

set var_sql = concat(var_sql,' GROUP BY origin_name');
set @sql = var_sql;
PREPARE stmt from @sql;
EXECUTE stmt;
END

-- 全部用户回款情况
DROP PROCEDURE IF EXISTS `TotalUserRepayRate`;
CREATE PROCEDURE `TotalUserRepayRate`(IN merch varchar(20),IN userSource varchar(20),IN userOrigin varchar(20),IN startTime varchar(20),IN endTime varchar(20))
BEGIN
SELECT
DATE_FORMAT(give_time,'%Y%m%d') as give_time,
DATE_FORMAT(DATE_ADD(give_time,INTERVAL +5 DAY),'%Y%m%d') AS nature_give_time,
CASE shmc
    WHEN 'mx' THEN '点滴'
    WHEN 'dawang' THEN '大王'
    WHEN 'huijie' THEN '惠借'
    WHEN 'haitun' THEN '海豚'
    WHEN 'fruit' THEN '火龙果'
    WHEN 'white' THEN '白龙马'
    WHEN 'have' THEN '全橙有借'
    WHEN 'parva' THEN '小鲸鱼'
    WHEN 'farm' THEN '惠农花'
    WHEN 'quick' THEN '快花花'
    WHEN 'lai' THEN '快来贷'
    WHEN 'time' THEN '时时钱包'
    WHEN 'fly' THEN '大象优借'
    WHEN 'net' THEN '网银分期'
    WHEN 'smile' THEN '妃子笑'
    WHEN 'loan' THEN '贷贷有财'
    WHEN 'opt' THEN '速亿通'
    WHEN 'fine' THEN '亿得金'
    WHEN 'bent' THEN '趣哪花'
    WHEN 'dit' THEN '信达钱包'
    WHEN 'spot' THEN '点来米'
    WHEN 'baby' THEN '宝宝钱包'
    WHEN 'come' THEN '马来钱包'
    WHEN 'oxen' THEN '小黄牛'
    WHEN 'rice' THEN '小灰机'
    WHEN 'mouse' THEN '牛牛袋子'
    WHEN 'big' THEN '大众钱袋'
    WHEN 'bloo' THEN '钱亿宝'
    WHEN 'free' THEN '有借无忧'
    WHEN 'vite' THEN 'V速'
    WHEN 'beer' THEN '啤酒花'
    WHEN 'moon' THEN '九月花'
    WHEN 'sth' THEN '小泡芙'
    ELSE '未知'
END AS merchant_name,

count(DISTINCT case WHEN give_time > 0 THEN  order_id ELSE null END) AS pay_count,
concat(ROUND(
                            (
                                count(DISTINCT case WHEN user_type IN (1,2,3) AND give_time > 0 THEN order_id ELSE null END)
                                -
                                count(DISTINCT case when user_type IN (1,2,3) AND give_time > 0 AND order_status IN (41) THEN order_id ELSE NULL END )
                                -
                                count(DISTINCT case when user_type IN (1,2,3) AND give_time > 0 AND order_status IN (35,37) AND zq_pay_status = 3 THEN order_id ELSE NULL END )
                            )
                                /
                                count(DISTINCT case WHEN user_type IN (1,2,3) AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),'%') time_total_over_rate,

concat(ROUND(
                                count(DISTINCT case when user_type IN (1,2,3) AND give_time > 0 AND order_status IN (33,34,37,38) THEN order_id ELSE NULL END )
                                /
                                count(DISTINCT case WHEN user_type IN (1,2,3) AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),'%') AS total_over_now_rate,

concat(ROUND(
                                count(DISTINCT case when user_type IN (1,2,3) AND give_time > 0 AND order_status IN (41,42,43) THEN order_id ELSE NULL END )
                                /
                                count(DISTINCT case WHEN user_type IN (1,2,3) AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),'%')  AS total_principal_pay_rate,

concat(ROUND(
                                count(DISTINCT case when user_type IN (1,2,3) AND give_time > 0 AND order_status IN (35,36) THEN order_id ELSE NULL END )
                                /
                                count(DISTINCT case WHEN user_type IN (1,2,3) AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),'%')  AS total_defer_rate,

count(DISTINCT case WHEN user_type = 1 AND give_time > 0 THEN order_id ELSE null END) AS new_user_count,
count(DISTINCT case WHEN user_type = 2 AND give_time > 0 THEN order_id ELSE null END) AS two_user_count,
concat(ROUND(
                            (
                                count(DISTINCT case WHEN user_type = 1 AND give_time > 0 THEN order_id ELSE null END)
                                -
                                count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (41) THEN order_id ELSE NULL END )
                                -
                                count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (35) AND zq_pay_status = 3 THEN order_id ELSE NULL END )
                            )
                                /
                                count(DISTINCT case WHEN user_type = 1 AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),'%') new_over_rate,

concat(ROUND(
                                count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (33,34,37,38) THEN order_id ELSE NULL END )
                                /
                                count(DISTINCT case WHEN user_type = 1 AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),'%') AS new_time_over_rate,

concat(ROUND(
                                count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (41,42,43) THEN order_id ELSE NULL END )
                                /
                                count(DISTINCT case WHEN user_type = 1 AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),'%')  AS new_principal_repay_rate,

concat(ROUND(
                                count(DISTINCT case when user_type = 1 AND give_time > 0 AND order_status IN (35,36) THEN order_id ELSE NULL END )
                                /
                                count(DISTINCT case WHEN user_type = 1 AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),'%')  AS new_defer_rate,

count(DISTINCT case WHEN user_type = 3 AND give_time > 0 THEN order_id ELSE null END) AS old_user_count,
concat(ROUND(
                            (
                                count(DISTINCT case WHEN user_type = 3 AND give_time > 0 THEN order_id ELSE null END)
                                -
                                count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (41) THEN order_id ELSE NULL END )
                                -
                                count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (35) AND zq_pay_status = 3 THEN order_id ELSE NULL END )
                            )
                                /
                                count(DISTINCT case WHEN user_type = 3 AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),'%') old_over_rate,

concat(ROUND(
                                count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (33,34,37,38) THEN order_id ELSE NULL END )
                                /
                                count(DISTINCT case WHEN user_type = 3 AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),'%') AS old_time_over_rate,

concat(ROUND(
                                count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (41,42,43) THEN order_id ELSE NULL END )
                                /
                                count(DISTINCT case WHEN user_type = 3 AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),'%')  AS old_principal_repay_rate,

concat(ROUND(
                                count(DISTINCT case when user_type = 3 AND give_time > 0 AND order_status IN (35,36) THEN order_id ELSE NULL END )
                                /
                                count(DISTINCT case WHEN user_type = 3 AND give_time > 0 THEN order_id ELSE null END)
                                *100,2),'%')  AS old_defer_rate

FROM
(
SELECT
*
FROM
(
SELECT
id AS order_id,order_no,uid,borrow_day,borrow_money,actual_money,interest_rate,interest_fee,overdue_rate,overdue_day,overdue_fee,should_repay,had_repay,reduce_money,`status` AS order_status,create_time AS order_ctime,audit_time,arrive_time AS give_time,repay_time,real_repay_time,merchant AS shmc,user_type,
DATE_FORMAT(DATE_ADD(repay_time,INTERVAL -1 DAY),'%Y%m%d') AS ahead_pay_day_1,
DATE_FORMAT(DATE_ADD(repay_time,INTERVAL +1 DAY),'%Y%m%d') AS behind_pay_day_1
FROM `tb_order`
)AS aa
LEFT JOIN
(
SELECT
id AS user_id,user_name,user_phone,user_cert_no,user_origin,create_time AS user_ctime
FROM
tb_user
)AS bb
ON aa.uid = bb.user_id
LEFT JOIN
(
SELECT
order_id AS rg_order_id,audit_id,audit_name,fail_reason,`status` AS rg_status,crete_time AS rg_ctime
FROM
tb_order_audit
)AS cc
ON aa.order_id = cc.rg_order_id
LEFT JOIN
(
SELECT
id AS zq_id,order_id AS zq_order_id,defer_day,defer_fee,defer_total_fee,defer_times,pay_type AS zq_pay_type,pay_no AS zq_pay_no,pay_status AS zq_pay_status,pay_time,create_time AS zq_create_time,repay_date AS old_should_pay_time,defer_repay_date
FROM
tb_order_defer
WHERE pay_status = 3
)AS dd
ON aa.order_id = dd.zq_order_id
WHERE user_phone NOT IN ('15757127746','18557530599','18838515567')  #### 去除测试人员
)AS abc
WHERE shmc = merch
-- AND uid IN (SELECT id FROM tb_user WHERE user_nick IN ('wechat'))  #### 限定来源  wechat,qq,pc,other,
-- AND user_origin IN (9)   #### 限定渠道
-- AND give_time > '2019-05-10 00:00:00' AND give_time < '2019-05-11 00:00:00'   #### 限定打款时间
-- AND DATE_FORMAT(DATE_ADD(give_time,INTERVAL +5 DAY),'%Y%m%d') < NOW()  #### 限定 只看当前到期的
GROUP BY 1,3
ORDER BY 3,1 DESC;
END
