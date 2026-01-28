-- 更新密码为明文
USE ai_medical;

UPDATE sys_user SET password = 'admin123' WHERE username = 'admin';
UPDATE sys_user SET password = 'doctor123' WHERE username = 'doctor';

-- 查看更新结果
SELECT username, password, real_name FROM sys_user;
