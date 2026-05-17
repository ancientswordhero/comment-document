INSERT IGNORE INTO categories (name, parent_id, sort_order) VALUES ('文学', NULL, 1);
INSERT IGNORE INTO categories (name, parent_id, sort_order) VALUES ('小说', 1, 1);
INSERT IGNORE INTO categories (name, parent_id, sort_order) VALUES ('诗词', 1, 2);
INSERT IGNORE INTO categories (name, parent_id, sort_order) VALUES ('散文', 1, 3);
INSERT IGNORE INTO categories (name, parent_id, sort_order) VALUES ('历史', NULL, 2);
INSERT IGNORE INTO categories (name, parent_id, sort_order) VALUES ('哲学', NULL, 3);
INSERT IGNORE INTO categories (name, parent_id, sort_order) VALUES ('科技', NULL, 4);
INSERT IGNORE INTO categories (name, parent_id, sort_order) VALUES ('艺术', NULL, 5);

INSERT IGNORE INTO books (title, author, isbn, category_id, description, status) VALUES
('红楼梦', '曹雪芹', '978-7-02-000220-9', 2, '<p>中国古典四大名著之一，以贾宝玉、林黛玉的爱情悲剧为主线。</p>', 1);
INSERT IGNORE INTO books (title, author, isbn, category_id, description, status) VALUES
('三体', '刘慈欣', '978-7-5366-9293-0', 7, '<p>地球文明向宇宙发出信号，引发了一场浩大的宇宙战争。</p>', 1);
INSERT IGNORE INTO books (title, author, isbn, category_id, description, status) VALUES
('史记', '司马迁', '978-7-101-00304-8', 5, '<p>中国第一部纪传体通史，记载了从黄帝到汉武帝的历史。</p>', 1);
INSERT IGNORE INTO books (title, author, isbn, category_id, description, status) VALUES
('论语', '孔子', '978-7-101-07034-7', 6, '<p>儒家经典，记录了孔子及其弟子的言行。</p>', 1);
