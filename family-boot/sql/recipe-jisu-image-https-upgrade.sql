-- 用途：将极速数据菜谱图片地址从 HTTP 升级为 HTTPS，解决小程序图片加载限制。
-- 执行范围：只替换 jisuapi 图片域名，不修改其他内容。

START TRANSACTION;

UPDATE baby_recipe
SET cover_url = REPLACE(
        REPLACE(cover_url, 'http://pic1.jisuapi.cn/', 'https://pic1.jisuapi.cn/'),
        'http://api.jisuapi.com/', 'https://api.jisuapi.com/'
    ),
    steps_json = REPLACE(
        REPLACE(steps_json, 'http://pic1.jisuapi.cn/', 'https://pic1.jisuapi.cn/'),
        'http://api.jisuapi.com/', 'https://api.jisuapi.com/'
    ),
    update_time = NOW()
WHERE is_deleted = 0
  AND (
    cover_url LIKE 'http://%jisuapi%'
    OR steps_json LIKE '%http://%jisuapi%'
  );

UPDATE recipe_source
SET cover_url = REPLACE(
        REPLACE(cover_url, 'http://pic1.jisuapi.cn/', 'https://pic1.jisuapi.cn/'),
        'http://api.jisuapi.com/', 'https://api.jisuapi.com/'
    ),
    update_time = NOW()
WHERE cover_url LIKE 'http://%jisuapi%';

COMMIT;
