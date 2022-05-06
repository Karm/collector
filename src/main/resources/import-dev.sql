USE `default`;
-- admin user for test/dev, his passwd is: This is my password.
INSERT INTO `user` VALUES (NEXTVAL(hibernate_sequence),NULL,'admin@collector.foci.life',-1,'$2a$10$46Y7jvv68ruK61qni5i3YOSllMkuusvEhD8ZmIVOHgCDKefqgrNh6','admin','admin');
INSERT INTO `user` VALUES (NEXTVAL(hibernate_sequence),NULL,'robot@collector.foci.life',-1,'$2a$10$87fbBnqq80bQKIp1N7iXBe82JhKUaWh.fzxSJYybYAdu2LW58fF.6','user','user');
