-- Optional development catalog. Product names and groupings were researched from the official
-- PlayStation, Xbox, and Nintendo catalogs in November 2026. Prices, editions, descriptions,
-- creation dates, and stock are deterministic synthetic fixtures, not live commercial offers.
-- Sources:
-- https://www.playstation.com/en-us/ps5/games/
-- https://www.playstation.com/en-us/accessories/
-- https://www.xbox.com/en-US/consoles
-- https://www.xbox.com/en-US/accessories
-- https://www.nintendo.com/us/store/games/nintendo-switch-games/
-- https://www.nintendo.com/us/store/hardware/

CREATE TEMPORARY TABLE demo_game_seed (
    seed_id INT NOT NULL AUTO_INCREMENT,
    title VARCHAR(180) NOT NULL,
    category_id INT NOT NULL,
    image_path VARCHAR(255) NOT NULL,
    PRIMARY KEY (seed_id)
);

INSERT INTO demo_game_seed (title, category_id, image_path) VALUES
('Astro Bot', 6, 'img/ps4.jpg'),
('Helldivers 2', 6, 'img/ps4.jpg'),
('Death Stranding 2: On the Beach', 6, 'img/ps4.jpg'),
('Demon''s Souls', 6, 'img/ps4.jpg'),
('Ghost of Tsushima Director''s Cut', 6, 'img/ps4.jpg'),
('Horizon Zero Dawn Remastered', 6, 'img/ps4.jpg'),
('Horizon Forbidden West', 6, 'img/ps4.jpg'),
('LEGO Horizon Adventures', 6, 'img/ps4.jpg'),
('Marvel''s Spider-Man: Miles Morales', 6, 'img/ps4.jpg'),
('Sackboy: A Big Adventure', 6, 'img/ps4.jpg'),
('The Last of Us Part I', 6, 'img/ps4.jpg'),
('The Last of Us Part II Remastered', 6, 'img/ps4.jpg'),
('Returnal', 6, 'img/ps4.jpg'),
('Rise of the Ronin', 6, 'img/ps4.jpg'),
('Uncharted: Legacy of Thieves Collection', 6, 'img/ps4.jpg'),
('Until Dawn', 6, 'img/ps4.jpg'),
('Gran Turismo 7', 6, 'img/ps4.jpg'),
('God of War Ragnarok', 6, 'img/ps4.jpg'),
('Ratchet & Clank: Rift Apart', 6, 'img/ps4.jpg'),
('Stellar Blade', 6, 'img/ps4.jpg'),
('Final Fantasy VII Rebirth', 6, 'img/ps4.jpg'),
('Marvel''s Spider-Man 2', 6, 'img/ps4.jpg'),
('Days Gone Remastered', 6, 'img/ps4.jpg'),
('Death Stranding Director''s Cut', 6, 'img/ps4.jpg'),
('Destruction AllStars', 6, 'img/ps4.jpg'),
('Forza Horizon 5', 9, 'img/xone.jpg'),
('Halo Infinite', 9, 'img/xone.jpg'),
('Starfield', 9, 'img/xone.jpg'),
('Indiana Jones and the Great Circle', 9, 'img/xone.jpg'),
('Avowed', 9, 'img/xone.jpg'),
('Grounded', 9, 'img/xone.jpg'),
('Sea of Thieves', 9, 'img/xone.jpg'),
('Microsoft Flight Simulator 2024', 9, 'img/xone.jpg'),
('Gears 5', 9, 'img/xone.jpg'),
('Doom: The Dark Ages', 9, 'img/xone.jpg'),
('Senua''s Saga: Hellblade II', 9, 'img/xone.jpg'),
('Age of Empires IV', 9, 'img/xone.jpg'),
('Minecraft Legends', 9, 'img/xone.jpg'),
('Hi-Fi Rush', 9, 'img/xone.jpg'),
('State of Decay 2', 9, 'img/xone.jpg'),
('Ori and the Will of the Wisps', 9, 'img/xone.jpg'),
('Psychonauts 2', 9, 'img/xone.jpg'),
('Quantum Break', 9, 'img/xone.jpg'),
('Killer Instinct', 9, 'img/xone.jpg'),
('Sunset Overdrive', 9, 'img/xone.jpg'),
('ReCore', 9, 'img/xone.jpg'),
('Tell Me Why', 9, 'img/xone.jpg'),
('As Dusk Falls', 9, 'img/xone.jpg'),
('Bleeding Edge', 9, 'img/xone.jpg'),
('Halo: The Master Chief Collection', 9, 'img/xone.jpg'),
('Super Mario Odyssey', 12, 'img/wii.jpg'),
('Mario Kart 8 Deluxe', 12, 'img/wii.jpg'),
('The Legend of Zelda: Breath of the Wild', 12, 'img/wii.jpg'),
('The Legend of Zelda: Tears of the Kingdom', 12, 'img/wii.jpg'),
('Animal Crossing: New Horizons', 12, 'img/wii.jpg'),
('Super Smash Bros. Ultimate', 12, 'img/wii.jpg'),
('Metroid Dread', 12, 'img/wii.jpg'),
('Pokemon Scarlet', 12, 'img/wii.jpg'),
('Pokemon Violet', 12, 'img/wii.jpg'),
('Splatoon 3', 12, 'img/wii.jpg'),
('Kirby and the Forgotten Land', 12, 'img/wii.jpg'),
('Luigi''s Mansion 3', 12, 'img/wii.jpg'),
('Super Mario Bros. Wonder', 12, 'img/wii.jpg'),
('Pikmin 4', 12, 'img/wii.jpg'),
('Fire Emblem Engage', 12, 'img/wii.jpg'),
('Xenoblade Chronicles 3', 12, 'img/wii.jpg'),
('Bayonetta 3', 12, 'img/wii.jpg'),
('Donkey Kong Country: Tropical Freeze', 12, 'img/wii.jpg'),
('Mario Party Superstars', 12, 'img/wii.jpg'),
('Nintendo Switch Sports', 12, 'img/wii.jpg'),
('Paper Mario: The Thousand-Year Door', 12, 'img/wii.jpg'),
('Princess Peach: Showtime!', 12, 'img/wii.jpg'),
('Mario vs. Donkey Kong', 12, 'img/wii.jpg'),
('Advance Wars 1+2: Re-Boot Camp', 12, 'img/wii.jpg'),
('Ring Fit Adventure', 12, 'img/wii.jpg');

INSERT INTO products (name, price, description, img, category_id, quantity, created_at)
WITH editions AS (
    SELECT 1 AS edition_id, 'Standard Edition' AS edition_name
    UNION ALL SELECT 2, 'Deluxe Edition'
    UNION ALL SELECT 3, 'Complete Edition'
    UNION ALL SELECT 4, 'Gold Edition'
    UNION ALL SELECT 5, 'Collector''s Edition'
    UNION ALL SELECT 6, 'Digital Bundle'
)
SELECT
    CONCAT(game.title, ' - ', edition.edition_name),
    CAST(79.90 + MOD(game.seed_id * 37 + edition.edition_id * 53, 320) AS DECIMAL(11,2)),
    'Synthetic development catalog item based on the official platform catalog; not a live offer.',
    game.image_path,
    game.category_id,
    5 + MOD(game.seed_id * 11 + edition.edition_id * 7, 46),
    DATE_SUB(NOW(), INTERVAL MOD(game.seed_id * 6 + edition.edition_id, 365) DAY)
FROM demo_game_seed game
CROSS JOIN editions edition;

DROP TEMPORARY TABLE demo_game_seed;

INSERT INTO products (name, price, description, img, category_id, quantity, created_at) VALUES
('PlayStation 5 Slim Disc Console', 3499.90, 'Synthetic development console fixture; not a live offer.', 'img/ps4.jpg', 5, 18, NOW()),
('PlayStation 5 Slim Digital Console', 2999.90, 'Synthetic development console fixture; not a live offer.', 'img/ps4.jpg', 5, 21, NOW()),
('PlayStation 5 Pro Console', 4999.90, 'Synthetic development console fixture; not a live offer.', 'img/ps4.jpg', 5, 9, NOW()),
('PlayStation 4 Slim Console', 1899.90, 'Synthetic development console fixture; not a live offer.', 'img/ps4.jpg', 5, 12, NOW()),
('PlayStation 5 Astro Bot Bundle', 3799.90, 'Synthetic development console fixture; not a live offer.', 'img/ps4.jpg', 5, 14, NOW()),
('PlayStation 5 Spider-Man Bundle', 3899.90, 'Synthetic development console fixture; not a live offer.', 'img/ps4.jpg', 5, 11, NOW()),
('PlayStation 5 Fortnite Bundle', 3699.90, 'Synthetic development console fixture; not a live offer.', 'img/ps4.jpg', 5, 13, NOW()),
('Xbox Series X 1TB Console', 3599.90, 'Synthetic development console fixture; not a live offer.', 'img/xone.jpg', 8, 17, NOW()),
('Xbox Series X 2TB Galaxy Black Console', 4499.90, 'Synthetic development console fixture; not a live offer.', 'img/xone.jpg', 8, 8, NOW()),
('Xbox Series X Digital Console', 3199.90, 'Synthetic development console fixture; not a live offer.', 'img/xone.jpg', 8, 15, NOW()),
('Xbox Series S 512GB Console', 2299.90, 'Synthetic development console fixture; not a live offer.', 'img/xone.jpg', 8, 24, NOW()),
('Xbox Series S 1TB Console', 2699.90, 'Synthetic development console fixture; not a live offer.', 'img/xone.jpg', 8, 19, NOW()),
('Xbox Series S Starter Bundle', 2499.90, 'Synthetic development console fixture; not a live offer.', 'img/xone.jpg', 8, 16, NOW()),
('Xbox Series X Diablo Bundle', 3899.90, 'Synthetic development console fixture; not a live offer.', 'img/xone.jpg', 8, 10, NOW()),
('Nintendo Switch 2 System', 3299.90, 'Synthetic development console fixture; not a live offer.', 'img/wii.jpg', 11, 20, NOW()),
('Nintendo Switch OLED White Console', 2499.90, 'Synthetic development console fixture; not a live offer.', 'img/wii.jpg', 11, 22, NOW()),
('Nintendo Switch OLED Neon Console', 2499.90, 'Synthetic development console fixture; not a live offer.', 'img/wii.jpg', 11, 19, NOW()),
('Nintendo Switch Neon Console', 2099.90, 'Synthetic development console fixture; not a live offer.', 'img/wii.jpg', 11, 26, NOW()),
('Nintendo Switch Lite Turquoise Console', 1499.90, 'Synthetic development console fixture; not a live offer.', 'img/wii.jpg', 11, 23, NOW()),
('Nintendo Switch Lite Coral Console', 1499.90, 'Synthetic development console fixture; not a live offer.', 'img/wii.jpg', 11, 18, NOW()),
('DualSense Wireless Controller', 449.90, 'Synthetic development accessory fixture; not a live offer.', 'img/ps4.jpg', 4, 35, NOW()),
('DualSense Edge Wireless Controller', 1299.90, 'Synthetic development accessory fixture; not a live offer.', 'img/ps4.jpg', 4, 16, NOW()),
('PlayStation Portal Remote Player', 1499.90, 'Synthetic development accessory fixture; not a live offer.', 'img/ps4.jpg', 4, 17, NOW()),
('PULSE Explore Wireless Earbuds', 1199.90, 'Synthetic development accessory fixture; not a live offer.', 'img/ps4.jpg', 4, 21, NOW()),
('PULSE Elite Wireless Headset', 899.90, 'Synthetic development accessory fixture; not a live offer.', 'img/ps4.jpg', 4, 25, NOW()),
('Access Controller for PlayStation 5', 599.90, 'Synthetic development accessory fixture; not a live offer.', 'img/ps4.jpg', 4, 12, NOW()),
('DualSense Charging Station', 249.90, 'Synthetic development accessory fixture; not a live offer.', 'img/ps4.jpg', 4, 31, NOW()),
('PlayStation 5 HD Camera', 399.90, 'Synthetic development accessory fixture; not a live offer.', 'img/ps4.jpg', 4, 22, NOW()),
('PlayStation 5 Media Remote', 199.90, 'Synthetic development accessory fixture; not a live offer.', 'img/ps4.jpg', 4, 28, NOW()),
('PlayStation 5 Disc Drive', 699.90, 'Synthetic development accessory fixture; not a live offer.', 'img/ps4.jpg', 4, 14, NOW()),
('Xbox Wireless Controller', 399.90, 'Synthetic development accessory fixture; not a live offer.', 'img/xone.jpg', 7, 42, NOW()),
('Xbox Elite Wireless Controller Series 2', 1099.90, 'Synthetic development accessory fixture; not a live offer.', 'img/xone.jpg', 7, 18, NOW()),
('Xbox Wireless Headset', 699.90, 'Synthetic development accessory fixture; not a live offer.', 'img/xone.jpg', 7, 25, NOW()),
('Xbox Stereo Headset', 349.90, 'Synthetic development accessory fixture; not a live offer.', 'img/xone.jpg', 7, 29, NOW()),
('Xbox Play and Charge Kit', 199.90, 'Synthetic development accessory fixture; not a live offer.', 'img/xone.jpg', 7, 36, NOW()),
('Xbox Adaptive Controller', 649.90, 'Synthetic development accessory fixture; not a live offer.', 'img/xone.jpg', 7, 15, NOW()),
('Xbox Series X Console Wrap', 299.90, 'Synthetic development accessory fixture; not a live offer.', 'img/xone.jpg', 7, 27, NOW()),
('Xbox 1TB Storage Expansion Card', 999.90, 'Synthetic development accessory fixture; not a live offer.', 'img/xone.jpg', 7, 19, NOW()),
('Xbox Mobile Gaming Clip', 149.90, 'Synthetic development accessory fixture; not a live offer.', 'img/xone.jpg', 7, 33, NOW()),
('Xbox Controller Charging Stand', 249.90, 'Synthetic development accessory fixture; not a live offer.', 'img/xone.jpg', 7, 24, NOW()),
('Nintendo Switch 2 Pro Controller', 599.90, 'Synthetic development accessory fixture; not a live offer.', 'img/wii.jpg', 10, 26, NOW()),
('Nintendo Switch Pro Controller', 449.90, 'Synthetic development accessory fixture; not a live offer.', 'img/wii.jpg', 10, 31, NOW()),
('Joy-Con 2 Blue and Light Yellow Pair', 649.90, 'Synthetic development accessory fixture; not a live offer.', 'img/wii.jpg', 10, 22, NOW()),
('Joy-Con Neon Red and Blue Pair', 549.90, 'Synthetic development accessory fixture; not a live offer.', 'img/wii.jpg', 10, 37, NOW()),
('Nintendo Switch 2 Camera', 349.90, 'Synthetic development accessory fixture; not a live offer.', 'img/wii.jpg', 10, 18, NOW()),
('Nintendo Switch Joy-Con Charging Stand', 219.90, 'Synthetic development accessory fixture; not a live offer.', 'img/wii.jpg', 10, 28, NOW()),
('Nintendo Switch Carrying Case', 149.90, 'Synthetic development accessory fixture; not a live offer.', 'img/wii.jpg', 10, 39, NOW()),
('Nintendo Switch 2 Carrying Case', 199.90, 'Synthetic development accessory fixture; not a live offer.', 'img/wii.jpg', 10, 32, NOW()),
('Nintendo Switch 256GB microSD Card', 349.90, 'Synthetic development accessory fixture; not a live offer.', 'img/wii.jpg', 10, 23, NOW()),
('Nintendo 64 Wireless Controller', 329.90, 'Synthetic development accessory fixture; not a live offer.', 'img/wii.jpg', 10, 17, NOW());
