SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `savedlocations`
-- ----------------------------
DROP TABLE IF EXISTS `savedlocations`;
CREATE TABLE `savedlocations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `characterid` int(11) NOT NULL,
  `locationtype` enum('FREE_MARKET','WORLDTOUR','FLORINA','INTRO','MIRROR','EVENT','AMORIA','MULUNG_TC') NOT NULL,
  `map` int(11) NOT NULL,
  `portal` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `savedlocations_ibfk_1` (`characterid`),
  CONSTRAINT `savedlocations_ibfk_1` FOREIGN KEY (`characterid`) REFERENCES `characters` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of savedlocations
-- ----------------------------