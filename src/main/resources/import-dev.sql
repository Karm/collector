-- Test data used for dev and test modes.
USE `quarkus`;

--CREATE TABLE user;

-- admin user for test/dev, his passwd is: This is my password.
INSERT INTO `user` (id, changePasswordTokenHash, email, lastLogin, password, role, username) VALUES (10, NULL, 'admin@collector.foci.life', -1, '$2a$10$46Y7jvv68ruK61qni5i3YOSllMkuusvEhD8ZmIVOHgCDKefqgrNh6', 'admin', 'admin');

-- Reset the auto-increment value for the `id` column in the `user` table
ALTER TABLE `user` AUTO_INCREMENT = 10;

-- Another user
INSERT INTO `user` (id, changePasswordTokenHash, email, lastLogin, password, role, username) VALUES (20, NULL, 'robot@collector.foci.life', -1, '$2a$10$87fbBnqq80bQKIp1N7iXBe82JhKUaWh.fzxSJYybYAdu2LW58fF.6', 'user', 'user');

-- Reset the auto-increment value for the `id` column in the `user` table
ALTER TABLE `user` AUTO_INCREMENT = 20;

-- Plaintext rw token for testing: f0ff6a4c91ed1ca1d3e01e118c5034c135a7fcce668f694b3995690108a1fcc6de78fcb6c0e438d70b6aca2475150c3f7724bac89b3270013f07d0105022e88e
INSERT INTO `token` VALUES ('rw',1,-1,20,'bace15255279fe71db0d8ad3d4ee7f2eff2871fbba2ffd62bad7f1fc5e8045b07436b41cb67a3924fa5033f4932434aa59d6ade8228df0d17bc98db1fdf38bfb');
