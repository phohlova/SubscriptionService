CREATE TABLE IF NOT EXISTS subscriptions (
                                             id BIGSERIAL PRIMARY KEY,
                                             user_id BIGINT NOT NULL,
                                             service_name VARCHAR(255) NOT NULL,
                                             status VARCHAR(20) NOT NULL,
                                             start_date TIMESTAMP NOT NULL,
                                             end_date TIMESTAMP NOT NULL,
                                             cost NUMERIC(19,2) NOT NULL,
                                             auto_renew BOOLEAN NOT NULL DEFAULT FALSE,
                                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_status ON subscriptions(status);
CREATE INDEX IF NOT EXISTS idx_subscriptions_start_date ON subscriptions(start_date);
CREATE INDEX IF NOT EXISTS idx_subscriptions_end_date ON subscriptions(end_date);