CREATE TABLE IF NOT EXISTS chat (
                      id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                      tg_id INT UNIQUE,
                      nickname VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS link (
                      id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                      url VARCHAR(255) UNIQUE,
                      last_update TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tag (
                     id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                     chat_id INT,
                     tag VARCHAR(255),
                     UNIQUE (chat_id, tag)
);

CREATE TABLE IF NOT EXISTS filter (
                        id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                        chat_id INT,
                        parameter VARCHAR(255),
                        value   VARCHAR(255),
                        UNIQUE (chat_id, parameter, value)
);

CREATE TABLE IF NOT EXISTS link_to_filter (
                                link_id INT,
                                filter_id INT,
                                PRIMARY KEY (link_id, filter_id)
);

CREATE TABLE IF NOT EXISTS link_to_chat (
                              chat_id INT,
                              link_id INT
);

CREATE TABLE IF NOT EXISTS tag_to_link (
                             link_id INT,
                             tag_id INT,
                             PRIMARY KEY (link_id, tag_id)
);

ALTER TABLE tag ADD FOREIGN KEY (user_id) REFERENCES chat (id) ON DELETE CASCADE;
ALTER TABLE filter ADD FOREIGN KEY (user_id) REFERENCES chat (id) ON DELETE CASCADE;

ALTER TABLE link_to_filter ADD FOREIGN KEY (link_id) REFERENCES link (id) ON DELETE CASCADE;

ALTER TABLE link_to_filter ADD FOREIGN KEY (filter_id) REFERENCES filter (id);

ALTER TABLE tag_to_link ADD FOREIGN KEY (tag_id) REFERENCES tag (id);

ALTER TABLE tag_to_link ADD FOREIGN KEY (link_id) REFERENCES link (id) ON DELETE CASCADE;

ALTER TABLE link_to_chat ADD FOREIGN KEY (link_id) REFERENCES link (id);

ALTER TABLE link_to_chat ADD FOREIGN KEY (user_id) REFERENCES chat (id) ON DELETE CASCADE;
