-- Test data used for dev and test modes.
USE `quarkus`;

CREATE TABLE user;

-- admin user for test/dev, his passwd is: This is my password.
INSERT INTO `user` (id, changePasswordTokenHash, email, lastLogin, password, role, username) VALUES (10, NULL, 'admin@collector.foci.life', -1, '$2a$10$46Y7jvv68ruK61qni5i3YOSllMkuusvEhD8ZmIVOHgCDKefqgrNh6', 'admin', 'admin');

-- Reset the auto-increment value for the `id` column in the `user` table
ALTER TABLE `user` AUTO_INCREMENT = 10;

-- Another user
INSERT INTO `user` (id, changePasswordTokenHash, email, lastLogin, password, role, username) VALUES (20, NULL, 'robot@collector.foci.life', -1, '$2a$10$87fbBnqq80bQKIp1N7iXBe82JhKUaWh.fzxSJYybYAdu2LW58fF.6', 'user', 'user');

-- Reset the auto-increment value for the `id` column in the `user` table
ALTER TABLE `user` AUTO_INCREMENT = 20;
