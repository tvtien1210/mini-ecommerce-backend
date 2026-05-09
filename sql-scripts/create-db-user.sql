-- xoá user nếu đã tồn tại
drop user if exists 'shopuser'@'%';
-- tạo user mới
-- % : nghia la có thể kết nối từ mọi host--
create user 'shopuser'@'%' identified by 'shoppass';
-- cấp quyền cho database shop_db
grant all privileges on shop_db.* to 'shopuser'@'%';
-- cập nhật quyền
flush privileges;