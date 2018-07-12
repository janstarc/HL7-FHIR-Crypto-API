-- phpMyAdmin SQL Dump
-- version 4.7.9
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jul 12, 2018 at 05:11 PM
-- Server version: 10.1.31-MariaDB
-- PHP Version: 7.2.3

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `cryptodb`
--
CREATE DATABASE IF NOT EXISTS `cryptodb` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `cryptodb`;

-- --------------------------------------------------------

--
-- Table structure for table `user_key`
--

DROP TABLE IF EXISTS `user_key`;
CREATE TABLE `user_key` (
  `user_id` int(11) NOT NULL,
  `key_alias` varchar(50) NOT NULL,
  `key_assigned` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `user_key`
--

INSERT INTO `user_key` (`user_id`, `key_alias`, `key_assigned`) VALUES
(11579, 'key6', '2018-07-12 22:04:30'),
(11587, 'key1', '2018-07-12 22:15:12'),
(11588, 'key12', '2018-07-12 22:19:36');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `user_key`
--
ALTER TABLE `user_key`
  ADD PRIMARY KEY (`user_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
