ALTER TABLE characters ADD profileEmotion int(11);
ALTER TABLE characters ADD profileConstellation int(11);
ALTER TABLE characters ADD profileBloodType int(11);
ALTER TABLE characters ADD profileBirthMonth int(11);
ALTER TABLE characters ADD profileBirthDay int(11);
ALTER TABLE characters ADD profileMessage varchar(128) CHARACTER SET utf8 NOT NULL DEFAULT '';