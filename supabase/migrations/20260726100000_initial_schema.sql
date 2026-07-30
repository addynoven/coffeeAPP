-- 1. Custom Enum Types
DO $$ BEGIN
    CREATE TYPE coffee_category AS ENUM ('ESPRESSO', 'LATTE', 'CAPPUCCINO', 'MACCHIATO', 'AMERICANO', 'ICED');
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    CREATE TYPE coffee_size AS ENUM ('SMALL', 'MEDIUM', 'LARGE');
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    CREATE TYPE discount_type AS ENUM ('PERCENTAGE', 'FIXED_AMOUNT');
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    CREATE TYPE order_status AS ENUM ('PREPARING', 'ON_THE_WAY', 'DELIVERED', 'CANCELLED');
EXCEPTION WHEN duplicate_object THEN null; END $$;

-- 2. Clean Up Tables (For Migration Idempotency)
DROP TABLE IF EXISTS search_history CASCADE;
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS addresses CASCADE;
DROP TABLE IF EXISTS cart CASCADE;
DROP TABLE IF EXISTS favorites CASCADE;
DROP TABLE IF EXISTS discounts CASCADE;
DROP TABLE IF EXISTS coffee CASCADE;

-- 3. Create Tables

-- Coffee Catalog
CREATE TABLE coffee (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL UNIQUE,
    description TEXT NOT NULL,
    category coffee_category NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    image_url TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    name_ja TEXT, description_ja TEXT,
    name_de TEXT, description_de TEXT,
    name_ru TEXT, description_ru TEXT,
    name_pt TEXT, description_pt TEXT,
    name_fr TEXT, description_fr TEXT,
    name_ar TEXT, description_ar TEXT,
    name_es TEXT, description_es TEXT,
    name_zh TEXT, description_zh TEXT,
    name_it TEXT, description_it TEXT
);

-- Discounts
CREATE TABLE discounts (
    code TEXT PRIMARY KEY,
    description TEXT NOT NULL,
    type discount_type NOT NULL,
    value NUMERIC(10,2) NOT NULL,
    min_order_amount NUMERIC(10,2),
    max_discount_amount NUMERIC(10,2),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Favorites
CREATE TABLE favorites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    coffee_id UUID NOT NULL REFERENCES coffee(id) ON DELETE CASCADE,
    UNIQUE(user_id, coffee_id)
);

-- Cart
CREATE TABLE cart (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    coffee_id UUID NOT NULL REFERENCES coffee(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL DEFAULT 1,
    size coffee_size NOT NULL
);

-- Addresses
CREATE TABLE addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    tag TEXT NOT NULL,
    full_address TEXT NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    last_used_timestamp BIGINT
);

-- Orders
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    total_price NUMERIC(10,2) NOT NULL,
    status order_status DEFAULT 'PREPARING',
    snapshot_address TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Order Items
CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    coffee_name TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    size coffee_size NOT NULL,
    snapshot_price NUMERIC(10,2) NOT NULL
);

-- Search History
CREATE TABLE search_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    query TEXT NOT NULL,
    result_count INTEGER NOT NULL,
    timestamp BIGINT
);

-- 4. Enable PowerSync WAL Publication
DROP PUBLICATION IF EXISTS powersync;
CREATE PUBLICATION powersync FOR TABLE
    coffee, discounts, favorites, cart, addresses, orders, order_items, search_history;

-- 5. Enable Row Level Security (RLS)
ALTER TABLE coffee ENABLE ROW LEVEL SECURITY;
ALTER TABLE discounts ENABLE ROW LEVEL SECURITY;
ALTER TABLE favorites ENABLE ROW LEVEL SECURITY;
ALTER TABLE cart ENABLE ROW LEVEL SECURITY;
ALTER TABLE addresses ENABLE ROW LEVEL SECURITY;
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE order_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE search_history ENABLE ROW LEVEL SECURITY;

-- 6. RLS Policies
CREATE POLICY "Allow public read on coffee"
    ON coffee FOR SELECT TO public USING (true);

CREATE POLICY "Allow public read on discounts"
    ON discounts FOR SELECT TO public USING (active = true);

CREATE POLICY "Users access own favorites"
    ON favorites FOR ALL TO authenticated
    USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users access own cart"
    ON cart FOR ALL TO authenticated
    USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users access own addresses"
    ON addresses FOR ALL TO authenticated
    USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users access own orders"
    ON orders FOR ALL TO authenticated
    USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users access own order items"
    ON order_items FOR ALL TO authenticated
    USING (EXISTS (
        SELECT 1 FROM orders
        WHERE orders.id = order_items.order_id
        AND orders.user_id = auth.uid()
    ))
    WITH CHECK (EXISTS (
        SELECT 1 FROM orders
        WHERE orders.id = order_items.order_id
        AND orders.user_id = auth.uid()
    ));

CREATE POLICY "Users access own search history"
    ON search_history FOR ALL TO authenticated
    USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- 7. Updated At Trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_coffee_updated_at
    BEFORE UPDATE ON coffee
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();