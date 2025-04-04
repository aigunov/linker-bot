CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS chat (
                      id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                      tg_id BIGINT UNIQUE,
                      nickname VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS link (
                      id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                      url VARCHAR(255) UNIQUE,
                      last_update TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tag (
                     id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                     chat_id UUID,
                     tag VARCHAR(255),
                     UNIQUE (chat_id, tag)
);

CREATE TABLE IF NOT EXISTS filter (
                        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        chat_id UUID,
                        parameter VARCHAR(255),
                        value   VARCHAR(255),
                        UNIQUE (chat_id, parameter, value)
);

CREATE TABLE IF NOT EXISTS link_to_filter (
                                link_id UUID,
                                filter_id UUID,
                                PRIMARY KEY (link_id, filter_id)
);

CREATE TABLE IF NOT EXISTS link_to_chat (
                              chat_id UUID,
                              link_id UUID,
                              PRIMARY KEY (chat_id, link_id)
);

CREATE TABLE IF NOT EXISTS tag_to_link (
                             link_id UUID,
                             tag_id UUID,
                             PRIMARY KEY (link_id, tag_id)
);

ALTER TABLE tag ADD FOREIGN KEY (chat_id) REFERENCES chat (id) ON DELETE CASCADE;
ALTER TABLE filter ADD FOREIGN KEY (chat_id) REFERENCES chat (id) ON DELETE CASCADE;

ALTER TABLE link_to_filter ADD FOREIGN KEY (link_id) REFERENCES link (id) ON DELETE CASCADE;

ALTER TABLE link_to_filter ADD FOREIGN KEY (filter_id) REFERENCES filter (id);

ALTER TABLE tag_to_link ADD FOREIGN KEY (tag_id) REFERENCES tag (id);

ALTER TABLE tag_to_link ADD FOREIGN KEY (link_id) REFERENCES link (id) ON DELETE CASCADE;

ALTER TABLE link_to_chat ADD FOREIGN KEY (link_id) REFERENCES link (id);

ALTER TABLE link_to_chat ADD FOREIGN KEY (chat_id) REFERENCES chat (id) ON DELETE CASCADE;
