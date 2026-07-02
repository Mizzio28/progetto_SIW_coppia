INSERT INTO armadietti (id, numero, dimensione)
SELECT n, n, CASE WHEN n % 2 = 0 THEN 'GRANDE' ELSE 'PICCOLO' END
FROM generate_series(1, 100) AS n
ON CONFLICT (id) DO NOTHING;
