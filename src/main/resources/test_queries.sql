SELECT SUM(s.pr_count),s.product_id FROM sklad s, product p WHERE s.product_id = p.id and p.name='Отвёртка' GROUP BY s.product_id;

SELECT COUNT(o.product_id) as Количество, p.name as Название FROM orders o
LEFT JOIN product p ON p.id = o.product_id
GROUP BY p.name ORDER BY Количество DESC LIMIT 3;

SELECT o.order_date as Дата, os.name as Статус, c.name as Имя, c.surname as Фамилия, pt.name as Категория,p.name as Продукт, o.pr_count as Количество FROM orders o LEFT JOIN customer c ON c.id = o.customer_id
LEFT JOIN product p ON p.id = o.product_id
LEFT JOIN order_status os ON os.id = o.order_status_id
LEFT JOIN product_type pt ON pt.id = p.product_type_id
ORDER BY o.order_date DESC LIMIT 5;

SELECT c.name, c.surname FROM customer c WHERE c.id NOT IN (select customer_id from orders);

SELECT p.name, p.cost, pt.name FROM product p, product_type pt WHERE cost = (select MAX(cost) from product) and p.product_type_id=pt.id;



UPDATE orders SET order_status_id=(select id from order_status where name='Готов') WHERE product_id=(select id from product where name='Минеральная вода') and customer_id=(select id from customer where phone='125') and order_status_id=(select id from order_status where name='Новый');

UPDATE sklad SET pr_count=106 WHERE product_id=(select id from product where name='Минеральная вода');

UPDATE customer SET email='as@df.ru' WHERE phone='125';


DELETE FROM customer WHERE id NOT IN (select customer_id from orders);

DELETE FROM product WHERE id NOT IN (select s.product_id from product p, sklad s where s.pr_count = 0 and s.product_id = p.id);