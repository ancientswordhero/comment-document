-- Notes and NoteLikes tables for 书余 feature
-- Extensions to reports and notifications for note support

CREATE TABLE IF NOT EXISTS notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    selected_text VARCHAR(500),
    cfi VARCHAR(500),
    type VARCHAR(20) NOT NULL DEFAULT 'INSIGHT',
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    parent_id BIGINT,
    root_id BIGINT,
    like_count INT NOT NULL DEFAULT 0,
    reply_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
);

CREATE TABLE IF NOT EXISTS note_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    note_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    UNIQUE KEY uk_note_user (note_id, user_id),
    FOREIGN KEY (note_id) REFERENCES notes(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Add note_id and target_type to reports (if not already added by JPA)
ALTER TABLE reports ADD COLUMN IF NOT EXISTS note_id BIGINT;
ALTER TABLE reports ADD COLUMN IF NOT EXISTS target_type VARCHAR(20) NOT NULL DEFAULT 'review';

-- Add note_id to notifications
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS note_id BIGINT;
