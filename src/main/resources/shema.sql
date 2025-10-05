/******************************************************************************/
/***                           БАЗА ДАННЫХ order_db                         ***/
/******************************************************************************/

CREATE DATABASE order_db
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'Russian_Russia.1251'
    LC_CTYPE = 'Russian_Russia.1251'
    LOCALE_PROVIDER = 'libc'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    IS_TEMPLATE = False;
	
/******************************************************************************/
/***           Сначала создаём генераторы значений полей id                 ***/
/******************************************************************************/

CREATE SEQUENCE product_gen_id INCREMENT 1;
CREATE SEQUENCE product_type_gen_id INCREMENT 1;
CREATE SEQUENCE customer_gen_id INCREMENT 1;
CREATE SEQUENCE order_status_gen_id INCREMENT 1;
CREATE SEQUENCE order_gen_id INCREMENT 1;

/******************************************************************************/
/***                Таблица product - описание продукта                     ***/
/******************************************************************************/

CREATE TABLE IF NOT EXISTS product (
id integer NOT NULL DEFAULT nextval('product_gen_id'),
name VARCHAR(200) NOT NULL ,
cost real NOT NULL CHECK (cost >= 0),
product_type_id integer NOT NULL ,
description text NULL ,
PRIMARY KEY ( id )
);

/******************************************************************************/
/***         Таблица product_type - перечисление типов продуктов            ***/
/******************************************************************************/

CREATE TABLE IF NOT EXISTS product_type (
id integer NOT NULL DEFAULT nextval('product_type_gen_id'),
name VARCHAR(200) NOT NULL ,
PRIMARY KEY ( id )
);

ALTER TABLE product ADD FOREIGN KEY ( product_type_id ) REFERENCES product_type ( id ) ON DELETE CASCADE ON UPDATE CASCADE ;

/******************************************************************************/
/***                Таблица customer - список заказчиков                    ***/
/******************************************************************************/

CREATE TABLE IF NOT EXISTS customer (
id integer NOT NULL DEFAULT nextval('customer_gen_id'),
name VARCHAR(40) NOT NULL ,
surname VARCHAR(40) NULL ,
email VARCHAR(100) NULL ,
phone VARCHAR(40) NOT NULL UNIQUE,
PRIMARY KEY ( id )
);

/******************************************************************************/
/***         Таблица order_status - список возможных статусов заказа        ***/
/******************************************************************************/

CREATE TABLE IF NOT EXISTS order_status (
id integer NOT NULL DEFAULT nextval('order_status_gen_id'),
name VARCHAR(40) NOT NULL ,
PRIMARY KEY ( id )
);

/******************************************************************************/
/***                      Таблица orders - заказы                           ***/
/******************************************************************************/

CREATE TABLE IF NOT EXISTS orders (
id integer NOT NULL DEFAULT nextval('order_gen_id'),
customer_id integer NOT NULL ,
product_id	integer NOT NULL ,
order_status_id	integer NOT NULL ,
pr_count integer NOT NULL CHECK (pr_count >= 0),
order_date TIMESTAMP NOT NULL ,
PRIMARY KEY ( id )
);

ALTER TABLE orders ADD FOREIGN KEY ( customer_id ) REFERENCES customer ( id ) ON DELETE CASCADE ON UPDATE CASCADE ;
ALTER TABLE orders ADD FOREIGN KEY ( product_id ) REFERENCES product ( id ) ON DELETE CASCADE ON UPDATE CASCADE ;
ALTER TABLE orders ADD FOREIGN KEY ( order_status_id ) REFERENCES order_status ( id ) ON DELETE CASCADE ON UPDATE CASCADE ;

/******************************************************************************/
/***                Таблица sklad - перечень товаров на складе              ***/
/******************************************************************************/

CREATE TABLE IF NOT EXISTS sklad (
product_id	integer NOT NULL ,
pr_count integer NOT NULL CHECK (pr_count >= 0),
PRIMARY KEY ( product_id )
);

ALTER TABLE orders ADD FOREIGN KEY ( product_id ) REFERENCES product ( id ) ON DELETE CASCADE ON UPDATE CASCADE ;

/******************************************************************************/
/***                      ИНДЕКСЫ для таблицы orders                        ***/
/******************************************************************************/

CREATE INDEX orders_order_date_index ON orders using btree(order_date);
CREATE INDEX orders_customer_id_index ON orders using btree(customer_id);
CREATE INDEX orders_product_id_index ON orders using btree(product_id);
CREATE INDEX orders_order_status_id_index ON orders using btree(order_status_id);
