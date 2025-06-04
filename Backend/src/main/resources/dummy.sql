INSERT INTO chess.users
(id, rating, game_id, password_hash, status, username)
VALUES(1, 1200, NULL, '$2a$10$JMu7RuyNa/u8Kir.SMxiZOZx4NfLrSTwPm8F3/.Q6q3QcfF8aEC5e', 'free', 'Ubay');
INSERT INTO chess.users
(id, rating, game_id, password_hash, status, username)
VALUES(2, 1200, NULL, '$2a$10$E9CXuDNXJYu0zpkVEXdIl.gXVFaV.KvTlg2PVe0jkUtAFDP8SHSte', 'free', '123');

INSERT INTO chess.chat_messages
(id, sent_at, user_id, message)
VALUES(1, '2025-06-03 17:48:37.395297', 1, 'test');