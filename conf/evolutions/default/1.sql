# --- First database schema

# --- !Ups
CREATE TABLE Trade_User (
  user_id varchar(255) NOT NULL,
  user_name varchar(255) NOT NULL,
  apikey varchar(255) NOT NULL,
  apisecret varchar(255) NOT NULL,
  asset decimal(10,0) NOT NULL DEFAULT '0',
  PRIMARY KEY (user_id),
  KEY apikey (apikey)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE Trade_Order (
  user_id varchar(255) NOT NULL,
  timestamp decimal(13,0) NOT NULL DEFAULT '0',
  side varchar(255) NOT NULL,
  price decimal(7,0) NOT NULL DEFAULT '0',
  size decimal(4,2) NOT NULL DEFAULT '0.00',
  orderId varchar(255) NOT NULL,
  PRIMARY KEY (orderId),
  KEY user_id (user_id),
  KEY timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE Trade_Open_Position (
  user_id varchar(255) NOT NULL,
  timestamp decimal(13,0) NOT NULL DEFAULT '0',
  side varchar(255) NOT NULL,
  price decimal(7,0) NOT NULL DEFAULT '0',
  size decimal(4,2) NOT NULL DEFAULT '0.00',
  orderId varchar(255) NOT NULL,
  KEY orderId (orderId),
  KEY user_id (user_id),
  KEY timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE Trade_Execution (
  id varchar(255) NOT NULL,
  timestamp decimal(13,0) NOT NULL DEFAULT '0',
  side varchar(255) NOT NULL,
  price decimal(7,0) NOT NULL DEFAULT '0',
  size decimal(4,2) NOT NULL DEFAULT '0.00',
  takerOrderId varchar(255) NOT NULL,
  orderId varchar(255) NOT NULL,
  PRIMARY KEY (id),
  KEY timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# --- !Downs
drop table if exists Trade_User;
drop table if exists Trade_Order;
drop table if exists Trade_Open_Position;
drop table if exists Trade_Execution;
