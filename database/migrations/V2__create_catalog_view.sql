CREATE VIEW `vw_category` AS SELECT
	c.id,
	concat(pai.`name`, ' > ', c.`name`) category_name
FROM
	category c
LEFT JOIN category pai ON (
	c.father_category_id = pai.id
)
WHERE
	c.father_category_id IS NOT NULL ;

