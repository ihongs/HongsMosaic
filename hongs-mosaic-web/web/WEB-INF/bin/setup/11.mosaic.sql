-- DB: mosaic

--
-- 数据
--

DROP TABLE IF EXISTS `a_mosaic_data`;
CREATE TABLE `a_mosaic_data` (
  `id` CHAR(16) NOT NULL,
  `form_id` CHAR(16) NOT NULL,
  `site_id` CHAR(16) NOT NULL,
  `user_id` CHAR(16) NOT NULL,
  `name` VARCHAR(255) DEFAULT NULL,
  `memo` VARCHAR(255) DEFAULT NULL,
  `data` LONGTEXT NOT NULL,
  `ctime` INTEGER(10) NOT NULL,
  `etime` INTEGER(10) NOT NULL,
  `rtime` INTEGER(10) DEFAULT NULL, /* 从哪个时间点恢复 */
  `state` TINYINT DEFAULT '1',
  PRIMARY KEY (`id`,`form_id`,`site_id`,`etime`)
);

CREATE INDEX `IK_a_mosaic_data_form` ON `a_mosaic_data` (`form_id`);
CREATE INDEX `IK_a_mosaic_data_site` ON `a_mosaic_data` (`site_id`);
CREATE INDEX `IK_a_mosaic_data_user` ON `a_mosaic_data` (`user_id`);
CREATE INDEX `IK_a_mosaic_data_state` ON `a_mosaic_data` (`state`);
CREATE INDEX `IK_a_mosaic_data_ctime` ON `a_mosaic_data` (`ctime`);
CREATE INDEX `IK_a_mosaic_data_etime` ON `a_mosaic_data` (`etime`);
CREATE INDEX `IK_a_mosaic_data_rtime` ON `a_mosaic_data` (`rtime`);
