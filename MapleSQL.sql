CREATE DATABASE  IF NOT EXISTS `maplestory` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `maplestory`;
-- MySQL dump 10.13  Distrib 5.7.9, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: maplestory
-- ------------------------------------------------------
-- Server version	5.7.11

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `accounts`
--

DROP TABLE IF EXISTS `accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `accounts` (
  `id` int(32) NOT NULL AUTO_INCREMENT,
  `username` varchar(16) NOT NULL,
  `password` varchar(128) NOT NULL,
  `salt` varchar(128) NOT NULL,
  `pic` varchar(16) NOT NULL DEFAULT '',
  `loggedin` tinyint(4) NOT NULL DEFAULT '0',
  `gm` int(32) NOT NULL DEFAULT '0',
  `nx_cash` int(11) NOT NULL DEFAULT '0',
  `maple_points` int(11) NOT NULL DEFAULT '0',
  `nx_prepaid` int(11) NOT NULL DEFAULT '0',
  `storage_meso` int(11) DEFAULT '0',
  `storage_size` int(11) DEFAULT '4',
  `login_message` varchar(300) DEFAULT '',
  `last_login` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`,`nx_prepaid`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cash_items`
--

DROP TABLE IF EXISTS `cash_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cash_items` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `account` int(11) NOT NULL,
  `itemid` int(11) NOT NULL,
  `unique_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cashshop_codes`
--

DROP TABLE IF EXISTS `cashshop_codes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cashshop_codes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(45) NOT NULL,
  `nx_cash` int(11) NOT NULL DEFAULT '0',
  `maple_points` int(11) NOT NULL DEFAULT '0',
  `nx_prepaid` int(11) NOT NULL DEFAULT '0',
  `item` int(11) NOT NULL DEFAULT '0',
  `item_amount` int(11) NOT NULL DEFAULT '0',
  `mesos` int(11) NOT NULL DEFAULT '0',
  `used` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `cashshop_codescol_UNIQUE` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `characters`
--

DROP TABLE IF EXISTS `characters`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `characters` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(25) NOT NULL,
  `level` smallint(6) NOT NULL DEFAULT '1',
  `world` tinyint(4) NOT NULL,
  `job` int(11) NOT NULL DEFAULT '0',
  `hair` int(11) NOT NULL DEFAULT '0',
  `skincolor` int(11) NOT NULL DEFAULT '0',
  `face` int(11) NOT NULL DEFAULT '0',
  `gender` tinyint(4) NOT NULL DEFAULT '0',
  `fame` smallint(6) NOT NULL DEFAULT '0',
  `str` smallint(6) NOT NULL DEFAULT '0',
  `int_` smallint(6) NOT NULL DEFAULT '0',
  `luk` smallint(6) NOT NULL DEFAULT '0',
  `dex` smallint(6) NOT NULL DEFAULT '0',
  `meso` int(11) NOT NULL DEFAULT '0',
  `map` int(11) NOT NULL DEFAULT '0',
  `owner` int(11) NOT NULL,
  `hp` smallint(6) NOT NULL DEFAULT '50',
  `maxhp` smallint(6) NOT NULL DEFAULT '50',
  `mp` smallint(6) NOT NULL DEFAULT '50',
  `maxmp` smallint(6) NOT NULL DEFAULT '50',
  `exp` int(11) NOT NULL DEFAULT '0',
  `ap` int(11) NOT NULL DEFAULT '0',
  `sp` int(11) NOT NULL DEFAULT '0',
  `fm_return_map` int(11) NOT NULL DEFAULT '100000000',
  `mount_tiredness` int(11) NOT NULL DEFAULT '0',
  `mount_exp` int(11) NOT NULL DEFAULT '0',
  `mount_level` int(11) NOT NULL DEFAULT '1',
  `last_fame` bigint(64) NOT NULL DEFAULT '0',
  `last_portal` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `owner` (`owner`)
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cooldowns`
--

DROP TABLE IF EXISTS `cooldowns`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cooldowns` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `owner` int(11) DEFAULT NULL,
  `skill_id` int(11) DEFAULT NULL,
  `delay` bigint(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `owner_idx` (`owner`),
  CONSTRAINT `owner` FOREIGN KEY (`owner`) REFERENCES `characters` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `duey_packages`
--

DROP TABLE IF EXISTS `duey_packages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `duey_packages` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sender` varchar(45) NOT NULL,
  `mesos` int(11) NOT NULL,
  `expiration_time` bigint(20) NOT NULL,
  `item` int(11) NOT NULL,
  `recipient` int(11) NOT NULL,
  `item_amount` int(11) NOT NULL,
  `item_data` varchar(350) DEFAULT NULL,
  `message` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `guild_bbs`
--

DROP TABLE IF EXISTS `guild_bbs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `guild_bbs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `guild` int(11) NOT NULL,
  `post_id` int(11) NOT NULL,
  `title` varchar(45) NOT NULL,
  `content` varchar(300) NOT NULL,
  `poster` int(11) NOT NULL,
  `post_time` bigint(20) NOT NULL,
  `notice` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `guild_bbs_guild_idx` (`guild`),
  KEY `guild_bbs_poster_idx` (`poster`),
  CONSTRAINT `guild_bbs_guild` FOREIGN KEY (`guild`) REFERENCES `guilds` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `guild_bbs_poster` FOREIGN KEY (`poster`) REFERENCES `characters` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `guild_entries`
--

DROP TABLE IF EXISTS `guild_entries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `guild_entries` (
  `character_id` int(11) NOT NULL,
  `guild` int(11) NOT NULL,
  `rank` int(11) NOT NULL,
  PRIMARY KEY (`character_id`),
  KEY `guild_id_guilds_idx` (`guild`),
  CONSTRAINT `character_id_guilds` FOREIGN KEY (`character_id`) REFERENCES `characters` (`id`) ON DELETE CASCADE,
  CONSTRAINT `guild_id_guilds` FOREIGN KEY (`guild`) REFERENCES `guilds` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `guilds`
--

DROP TABLE IF EXISTS `guilds`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `guilds` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `world` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `notice` varchar(45) NOT NULL,
  `capacity` int(11) NOT NULL DEFAULT '10',
  `emblem_background` int(11) NOT NULL DEFAULT '0',
  `emblem_background_color` int(11) NOT NULL DEFAULT '0',
  `emblem_logo` int(11) NOT NULL DEFAULT '0',
  `emblem_logo_color` int(11) NOT NULL DEFAULT '0',
  `creation_time` bigint(20) NOT NULL,
  `points` int(11) NOT NULL DEFAULT '0',
  `rank_master` varchar(45) NOT NULL DEFAULT 'Master',
  `rank_jr_master` varchar(45) NOT NULL DEFAULT 'JrMaster',
  `rank_member_1` varchar(45) NOT NULL DEFAULT 'Member',
  `rank_member_2` varchar(45) NOT NULL DEFAULT 'Member',
  `rank_member_3` varchar(45) NOT NULL DEFAULT 'Member',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `inventory_items`
--

DROP TABLE IF EXISTS `inventory_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inventory_items` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `inventory_type` int(11) NOT NULL,
  `player` int(11) NOT NULL,
  `slot` int(11) DEFAULT NULL,
  `itemid` int(11) NOT NULL,
  `amount` int(11) NOT NULL,
  `owner` varchar(45) NOT NULL,
  `flag` int(11) NOT NULL DEFAULT '0',
  `expiration` bigint(20) NOT NULL,
  `unique_id` bigint(20) NOT NULL,
  `data` varchar(350) DEFAULT NULL COMMENT 'The max size data can possible be is 341. That is with the values set like this {acc=2147483647, luk=2147483647, mp=2147483647, hands=2147483647, level=2147483647, hp=2147483647, mdef=2147483647, int=2147483647, speed=2147483647, str=2147483647, dex=2147483647, watk=2147483647, hammerUpgrades=2147483647, matk=2147483647, wdef=2147483647, upgradeSlots=2147483647, avoid=2147483647, jump=2147483647, itemLevel=2147483647}. I''ve decided to make the VARCHAR size 350 as a ''safe guard'' in case the size increases a little. May change to TEXT in the future.',
  PRIMARY KEY (`id`),
  KEY `item_owner_player_id_idx` (`player`),
  CONSTRAINT `item_owner_player_id` FOREIGN KEY (`player`) REFERENCES `characters` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=71745 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `keybindings`
--

DROP TABLE IF EXISTS `keybindings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `keybindings` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `character` int(11) DEFAULT NULL,
  `key` int(11) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `action` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `keybindings_ibfk_1` (`character`),
  CONSTRAINT `keybindings_ibfk_1` FOREIGN KEY (`character`) REFERENCES `characters` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `party_quest`
--

DROP TABLE IF EXISTS `party_quest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `party_quest` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `player` int(11) NOT NULL,
  `rice_cakes_given` int(11) NOT NULL DEFAULT '0',
  `rice_hat_given` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `player_UNIQUE` (`player`),
  KEY `pq_player_id_idx` (`player`),
  CONSTRAINT `pq_player_id` FOREIGN KEY (`player`) REFERENCES `characters` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `quest_progress`
--

DROP TABLE IF EXISTS `quest_progress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `quest_progress` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `owner` int(11) NOT NULL,
  `questid` int(11) NOT NULL,
  `progress_id` int(11) NOT NULL,
  `progress` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `owner_id_idx` (`owner`),
  CONSTRAINT `progress_owner_id` FOREIGN KEY (`owner`) REFERENCES `characters` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=143 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `quests`
--

DROP TABLE IF EXISTS `quests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `quests` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `owner` int(11) NOT NULL,
  `questid` int(11) NOT NULL,
  `state` int(11) NOT NULL,
  `forfeit` int(11) NOT NULL DEFAULT '0',
  `completion_time` bigint(20) NOT NULL,
  `npc` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `owner_idx` (`owner`),
  CONSTRAINT `quests_owner_id` FOREIGN KEY (`owner`) REFERENCES `characters` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6505 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `shopitems`
--

DROP TABLE IF EXISTS `shopitems`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shopitems` (
  `shopitemid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `shopid` int(10) unsigned NOT NULL,
  `itemid` int(11) NOT NULL,
  `price` int(11) NOT NULL,
  `pitch` int(11) NOT NULL DEFAULT '0',
  `position` int(11) NOT NULL COMMENT 'sort is an arbitrary field designed to give leeway when modifying shops. The lowest number is 104 and it increments by 4 for each item to allow decent space for swapping/inserting/removing items.',
  PRIMARY KEY (`shopitemid`)
) ENGINE=MyISAM AUTO_INCREMENT=20047 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `shops`
--

DROP TABLE IF EXISTS `shops`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shops` (
  `shopid` int(11) NOT NULL,
  `npcid` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`shopid`)
) ENGINE=MyISAM AUTO_INCREMENT=10000000 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `skills`
--

DROP TABLE IF EXISTS `skills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `skills` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `owner` int(11) NOT NULL,
  `skillid` int(11) NOT NULL,
  `level` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `skills_ibfk_1` (`owner`),
  CONSTRAINT `skills_ibfk_1` FOREIGN KEY (`owner`) REFERENCES `characters` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=740 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `storage_item_equip_data`
--

DROP TABLE IF EXISTS `storage_item_equip_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `storage_item_equip_data` (
  `inventory_id` int(10) NOT NULL,
  `upgradeslots` int(11) NOT NULL DEFAULT '0',
  `level` int(11) NOT NULL DEFAULT '0',
  `str` int(11) NOT NULL DEFAULT '0',
  `dex` int(11) NOT NULL DEFAULT '0',
  `int` int(11) NOT NULL DEFAULT '0',
  `luk` int(11) NOT NULL DEFAULT '0',
  `hp` int(11) NOT NULL DEFAULT '0',
  `mp` int(11) NOT NULL DEFAULT '0',
  `watk` int(11) NOT NULL DEFAULT '0',
  `matk` int(11) NOT NULL DEFAULT '0',
  `wdef` int(11) NOT NULL DEFAULT '0',
  `mdef` int(11) NOT NULL DEFAULT '0',
  `acc` int(11) NOT NULL DEFAULT '0',
  `avoid` int(11) NOT NULL DEFAULT '0',
  `hands` int(11) NOT NULL DEFAULT '0',
  `speed` int(11) NOT NULL DEFAULT '0',
  `jump` int(11) NOT NULL DEFAULT '0',
  `hammer` int(10) NOT NULL DEFAULT '0',
  `itemlevel` int(11) NOT NULL DEFAULT '1',
  `itemexp` int(11) NOT NULL DEFAULT '0',
  `ringid` int(11) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`inventory_id`),
  KEY `item_id_idx` (`inventory_id`),
  CONSTRAINT `storage_item_id` FOREIGN KEY (`inventory_id`) REFERENCES `storage_items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `storage_items`
--

DROP TABLE IF EXISTS `storage_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `storage_items` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `inventory_type` int(11) NOT NULL,
  `account` int(11) NOT NULL,
  `slot` int(11) DEFAULT NULL,
  `itemid` int(11) NOT NULL,
  `amount` int(11) NOT NULL,
  `owner` varchar(45) NOT NULL,
  `flag` int(11) NOT NULL DEFAULT '0',
  `expiration` bigint(20) NOT NULL,
  `unique_id` bigint(20) NOT NULL,
  `pet_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `item_owner_player_id_idx` (`account`)
) ENGINE=InnoDB AUTO_INCREMENT=68 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-12-12 10:18:18
