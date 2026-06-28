CREATE DATABASE IF NOT EXISTS hyy_collision
  DEFAULT CHARACTER SET ascii
  DEFAULT COLLATE ascii_bin;

USE hyy_collision;

CREATE TABLE IF NOT EXISTS hyy_collision_mobile (
  phone_prefix8 CHAR(8) NOT NULL,
  mobile_md5 CHAR(32) NOT NULL,
  batch_no VARCHAR(64) NOT NULL DEFAULT '',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (phone_prefix8, mobile_md5),
  KEY idx_mobile_md5 (mobile_md5)
) ENGINE=InnoDB DEFAULT CHARSET=ascii COLLATE=ascii_bin;

CREATE TABLE IF NOT EXISTS hyy_collision_identity (
  phone_prefix8 CHAR(8) NOT NULL,
  name_md5 CHAR(32) NOT NULL DEFAULT '',
  idno_md5 CHAR(32) NOT NULL DEFAULT '',
  batch_no VARCHAR(64) NOT NULL DEFAULT '',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (phone_prefix8, name_md5, idno_md5),
  KEY idx_prefix_idno (phone_prefix8, idno_md5)
) ENGINE=InnoDB DEFAULT CHARSET=ascii COLLATE=ascii_bin;
