-- SQL script to insert 20 test users with varied statistics for testing leaderboard rankings
-- Note: These users have randomized statistics for points, study_time, games_played, and strike

-- Insert 20 test users
INSERT INTO users (
    user_frn,
    username,
    first_name,
    last_name,
    email,
    password,
    created_at,
    updated_at,
    enabled,
    activation_token,
    study_time,
    games_played,
    points,
    strike,
    friends_frn
) VALUES
-- User 1 - High points, medium study time, low games, high streak
(
    'frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f001',
    'testuser1',
    'Alex',
    'Johnson',
    'test1@flashdash.com',
    '$2a$10$HPEqP2BI5W0r/sLBUysPWuv5Hg3oLh0FdkMXQLVbWI7WuS51nlcAW',
    '2025-03-20 09:04:04.216825',
    '2025-03-20 09:04:04.216825',
    true,
    '84d93051-1a8f-4c26-b81b-9f45a5019001',
    51622066000, -- 14.3 hours in milliseconds
    15,
    2500,
    28,
    '["frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f002", "frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f003"]'
),

-- User 2 - Very high points, low study time, medium games, medium streak
(
    'frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f002',
    'testuser2',
    'Emily',
    'Smith',
    'test2@flashdash.com',
    '$2a$10$HPEqP2BI5W0r/sLBUysPWuv5Hg3oLh0FdkMXQLVbWI7WuS51nlcAW',
    '2025-03-20 10:14:24.216825',
    '2025-03-20 10:14:24.216825',
    true,
    '84d93051-1a8f-4c26-b81b-9f45a5019002',
    28822066000, -- 8 hours in milliseconds
    35,
    3800,
    15,
    '["frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f001", "frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f004"]'
),

-- User 3 - Medium points, high study time, medium games, low streak
(
    'frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f003',
    'testuser3',
    'Michael',
    'Brown',
    'test3@flashdash.com',
    '$2a$10$HPEqP2BI5W0r/sLBUysPWuv5Hg3oLh0FdkMXQLVbWI7WuS51nlcAW',
    '2025-03-20 11:24:44.216825',
    '2025-03-20 11:24:44.216825',
    true,
    '84d93051-1a8f-4c26-b81b-9f45a5019003',
    72022066000, -- 20 hours in milliseconds
    30,
    1800,
    8,
    '["frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f001", "frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f005"]'
),

-- User 4 - Low points, very high study time, high games, medium streak
(
    'frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f004',
    'testuser4',
    'Sarah',
    'Davis',
    'test4@flashdash.com',
    '$2a$10$HPEqP2BI5W0r/sLBUysPWuv5Hg3oLh0FdkMXQLVbWI7WuS51nlcAW',
    '2025-03-20 12:35:04.216825',
    '2025-03-20 12:35:04.216825',
    true,
    '84d93051-1a8f-4c26-b81b-9f45a5019004',
    108022066000, -- 30 hours in milliseconds
    60,
    950,
    12,
    '["frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f002", "frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f006"]'
),

-- User 5 - High points, high study time, very high games, very high streak
(
    'frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f005',
    'testuser5',
    'James',
    'Wilson',
    'test5@flashdash.com',
    '$2a$10$HPEqP2BI5W0r/sLBUysPWuv5Hg3oLh0FdkMXQLVbWI7WuS51nlcAW',
    '2025-03-20 13:45:24.216825',
    '2025-03-20 13:45:24.216825',
    true,
    '84d93051-1a8f-4c26-b81b-9f45a5019005',
    86422066000, -- 24 hours in milliseconds
    85,
    3200,
    32,
    '["frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f003", "frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f007"]'
),

-- User 6 - Medium points, medium study time, medium games, high streak
(
    'frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f006',
    'testuser6',
    'Jessica',
    'Martinez',
    'test6@flashdash.com',
    '$2a$10$HPEqP2BI5W0r/sLBUysPWuv5Hg3oLh0FdkMXQLVbWI7WuS51nlcAW',
    '2025-03-20 14:55:44.216825',
    '2025-03-20 14:55:44.216825',
    true,
    '84d93051-1a8f-4c26-b81b-9f45a5019006',
    43222066000, -- 12 hours in milliseconds
    40,
    1950,
    25,
    '["frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f004", "frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f008"]'
),

-- User 7 - Very high points, medium study time, low games, medium streak
(
    'frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f007',
    'testuser7',
    'David',
    'Anderson',
    'test7@flashdash.com',
    '$2a$10$HPEqP2BI5W0r/sLBUysPWuv5Hg3oLh0FdkMXQLVbWI7WuS51nlcAW',
    '2025-03-20 15:06:04.216825',
    '2025-03-20 15:06:04.216825',
    true,
    '84d93051-1a8f-4c26-b81b-9f45a5019007',
    54022066000, -- 15 hours in milliseconds
    20,
    4100,
    18,
    '["frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f005", "frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f009"]'
),

-- User 8 - Low points, low study time, high games, very low streak
(
    'frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f008',
    'testuser8',
    'Lisa',
    'Taylor',
    'test8@flashdash.com',
    '$2a$10$HPEqP2BI5W0r/sLBUysPWuv5Hg3oLh0FdkMXQLVbWI7WuS51nlcAW',
    '2025-03-20 16:16:24.216825',
    '2025-03-20 16:16:24.216825',
    true,
    '84d93051-1a8f-4c26-b81b-9f45a5019008',
    18022066000, -- 5 hours in milliseconds
    65,
    750,
    3,
    '["frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f006", "frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f010"]'
),

-- User 9 - Medium points, very low study time, medium games, low streak
(
    'frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f009',
    'testuser9',
    'Robert',
    'Thomas',
    'test9@flashdash.com',
    '$2a$10$HPEqP2BI5W0r/sLBUysPWuv5Hg3oLh0FdkMXQLVbWI7WuS51nlcAW',
    '2025-03-20 17:26:44.216825',
    '2025-03-20 17:26:44.216825',
    true,
    '84d93051-1a8f-4c26-b81b-9f45a5019009',
    10822066000, -- 3 hours in milliseconds
    45,
    2100,
    7,
    '["frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f007", "frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f011"]'
),

-- User 10 - Highest points, highest study time, highest games, highest streak
(
    'frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f010',
    'testuser10',
    'Amanda',
    'Garcia',
    'test10@flashdash.com',
    '$2a$10$HPEqP2BI5W0r/sLBUysPWuv5Hg3oLh0FdkMXQLVbWI7WuS51nlcAW',
    '2025-03-20 18:37:04.216825',
    '2025-03-20 18:37:04.216825',
    true,
    '84d93051-1a8f-4c26-b81b-9f45a5019010',
    144022066000, -- 40 hours in milliseconds
    100,
    5000,
    40,
    '["frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f008", "frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f012"]'
),

-- User 11 - Low points, medium study time, low games, medium streak
(
    'frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f011',
    'testuser11',
    'Kevin',
    'Lee',
    'test11@flashdash.com',
    '$2a$10$HPEqP2BI5W0r/sLBUysPWuv5Hg3oLh0FdkMXQLVbWI7WuS51nlcAW',
    '2025-03-20 19:47:24.216825',
    '2025-03-20 19:47:24.216825',
    true,
    '84d93051-1a8f-4c26-b81b-9f45a5019011',
    39622066000, -- 11 hours in milliseconds
    18,
    850,
    16,
    '["frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f009", "frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f013"]'
),

-- User 12 - High points, low study time, very high games, low streak
(
    'frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f012',
    'testuser12',
    'Michelle',
    'Clark',
    'test12@flashdash.com',
    '$2a$10$HPEqP2BI5W0r/sLBUysPWuv5Hg3oLh0FdkMXQLVbWI7WuS51nlcAW',
    '2025-03-20 20:57:44.216825',
    '2025-03-20 20:57:44.216825',
    true,
    '84d93051-1a8f-4c26-b81b-9f45a5019012',
    21622066000, -- 6 hours in milliseconds
    80,
    2800,
    9,
    '["frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f010", "frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f014"]'
),

-- User 13 - Very low points, high study time, medium games, very high streak
(
    'frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f013',
    'testuser13',
    'Daniel',
    'Rodriguez',
    'test13@flashdash.com',
    '$2a$10$HPEqP2BI5W0r/sLBUysPWuv5Hg3oLh0FdkMXQLVbWI7WuS51nlcAW',
    '2025-03-20 21:08:04.216825',
    '2025-03-20 21:08:04.216825',
    true,
    '84d93051-1a8f-4c26-b81b-9f45a5019013',
    79222066000, -- 22 hours in milliseconds
    35,
    480,
    35,
    '["frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f011", "frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f015"]'
),

-- User 14 - Medium points, high study time, low games, high streak
(
    'frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f014',
    'testuser14',
    'Lauren',
    'Walker',
    'test14@flashdash.com',
    '$2a$10$HPEqP2BI5W0r/sLBUysPWuv5Hg3oLh0FdkMXQLVbWI7WuS51nlcAW',
    '2025-03-20 22:18:24.216825',
    '2025-03-20 22:18:24.216825',
    true,
    '84d93051-1a8f-4c26-b81b-9f45a5019014',
    68422066000, -- 19 hours in milliseconds
    12,
    2250,
    27,
    '["frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f012", "frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f016"]'
),

-- User 15 - Very low points, medium study time, very low games, medium streak
(
    'frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f015',
    'testuser15',
    'Ryan',
    'Hall',
    'test15@flashdash.com',
    '$2a$10$HPEqP2BI5W0r/sLBUysPWuv5Hg3oLh0FdkMXQLVbWI7WuS51nlcAW',
    '2025-03-20 23:28:44.216825',
    '2025-03-20 23:28:44.216825',
    true,
    '84d93051-1a8f-4c26-b81b-9f45a5019015',
    46822066000, -- 13 hours in milliseconds
    8,
    320,
    14,
    '["frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f013", "frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f017"]'
),

-- User 16 - Low points, very high study time, medium games, low streak
(
    'frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f016',
    'testuser16',
    'Hannah',
    'Allen',
    'test16@flashdash.com',
    '$2a$10$HPEqP2BI5W0r/sLBUysPWuv5Hg3oLh0FdkMXQLVbWI7WuS51nlcAW',
    '2025-03-21 00:39:04.216825',
    '2025-03-21 00:39:04.216825',
    true,
    '84d93051-1a8f-4c26-b81b-9f45a5019016',
    93622066000, -- 26 hours in milliseconds
    32,
    680,
    6,
    '["frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f014", "frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f018"]'
),

-- User 17 - High points, medium study time, high games, medium streak
(
    'frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f017',
    'testuser17',
    'Tyler',
    'Young',
    'test17@flashdash.com',
    '$2a$10$HPEqP2BI5W0r/sLBUysPWuv5Hg3oLh0FdkMXQLVbWI7WuS51nlcAW',
    '2025-03-21 01:49:24.216825',
    '2025-03-21 01:49:24.216825',
    true,
    '84d93051-1a8f-4c26-b81b-9f45a5019017',
    57622066000, -- 16 hours in milliseconds
    55,
    2950,
    19,
    '["frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f015", "frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f019"]'
),

-- User 18 - Medium points, very low study time, very high games, low streak
(
    'frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f018',
    'testuser18',
    'Nicole',
    'King',
    'test18@flashdash.com',
    '$2a$10$HPEqP2BI5W0r/sLBUysPWuv5Hg3oLh0FdkMXQLVbWI7WuS51nlcAW',
    '2025-03-21 02:59:44.216825',
    '2025-03-21 02:59:44.216825',
    true,
    '84d93051-1a8f-4c26-b81b-9f45a5019018',
    14422066000, -- 4 hours in milliseconds
    75,
    1700,
    5,
    '["frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f016", "frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f020"]'
),

-- User 19 - Low points, low study time, low games, very high streak
(
    'frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f019',
    'testuser19',
    'Brandon',
    'Wright',
    'test19@flashdash.com',
    '$2a$10$HPEqP2BI5W0r/sLBUysPWuv5Hg3oLh0FdkMXQLVbWI7WuS51nlcAW',
    '2025-03-21 03:10:04.216825',
    '2025-03-21 03:10:04.216825',
    true,
    '84d93051-1a8f-4c26-b81b-9f45a5019019',
    25222066000, -- 7 hours in milliseconds
    25,
    520,
    30,
    '["frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f017", "frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f001"]'
),

-- User 20 - Medium-high points, medium-high study time, medium-high games, medium-high streak
(
    'frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f020',
    'testuser20',
    'Olivia',
    'Scott',
    'test20@flashdash.com',
    '$2a$10$HPEqP2BI5W0r/sLBUysPWuv5Hg3oLh0FdkMXQLVbWI7WuS51nlcAW',
    '2025-03-21 04:20:24.216825',
    '2025-03-21 04:20:24.216825',
    true,
    '84d93051-1a8f-4c26-b81b-9f45a5019020',
    64822066000, -- 18 hours in milliseconds
    50,
    2600,
    22,
    '["frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f018", "frn:flashdash:user:01f41151-679d-40dd-a6dd-321a2d99f002"]'
);