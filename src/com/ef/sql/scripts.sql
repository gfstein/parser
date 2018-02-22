# find IPs that mode more than a certain number of requests for a given time period
SELECT al.ip
FROM access_log al
WHERE al.access_date BETWEEN :initialData AND :finalDate
GROUP BY al.ip HAVING count(al.ip) >= :threshold;

# find requests made by a given IP
SELECT *
FROM access_log al
WHERE al.ip = :ip;