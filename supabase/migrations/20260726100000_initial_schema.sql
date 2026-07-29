-- Initial Schema Migration

-- 1. Coffee Catalog (Global)
CREATE TABLE IF NOT EXISTS coffee (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    description TEXT NOT NULL,
    category TEXT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    image_url TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 2. Favorites (User Specific)
CREATE TABLE IF NOT EXISTS favorites (
    id SERIAL PRIMARY KEY,
    user_id TEXT NOT NULL,
    coffee_id INTEGER REFERENCES coffee(id) ON DELETE CASCADE,
    UNIQUE(user_id, coffee_id)
);

-- 3. Cart
CREATE TABLE IF NOT EXISTS cart (
    id SERIAL PRIMARY KEY,
    user_id TEXT NOT NULL,
    coffee_id INTEGER REFERENCES coffee(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL DEFAULT 1,
    size TEXT NOT NULL
);

-- 4. Addresses
CREATE TABLE IF NOT EXISTS addresses (
    id SERIAL PRIMARY KEY,
    user_id TEXT NOT NULL,
    tag TEXT NOT NULL,
    full_address TEXT NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    last_used_timestamp BIGINT
);

-- 5. Orders & Order Items
CREATE TABLE IF NOT EXISTS orders (
    id SERIAL PRIMARY KEY,
    user_id TEXT NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    status TEXT DEFAULT 'Preparing',
    snapshot_address TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS order_items (
    id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(id) ON DELETE CASCADE,
    coffee_name TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    size TEXT NOT NULL,
    snapshot_price DECIMAL(10,2) NOT NULL
);

-- 6. Search History
CREATE TABLE IF NOT EXISTS search_history (
    id SERIAL PRIMARY KEY,
    user_id TEXT NOT NULL,
    query TEXT NOT NULL,
    result_count INTEGER NOT NULL,
    timestamp BIGINT
);

-- Row Level Security (RLS)
ALTER TABLE coffee ENABLE ROW LEVEL SECURITY;
ALTER TABLE favorites ENABLE ROW LEVEL SECURITY;
ALTER TABLE cart ENABLE ROW LEVEL SECURITY;
ALTER TABLE addresses ENABLE ROW LEVEL SECURITY;
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE order_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE search_history ENABLE ROW LEVEL SECURITY;

-- Policies
CREATE POLICY "Allow public read on coffee" ON coffee FOR SELECT USING (true);
CREATE POLICY "Allow anon insert on coffee" ON coffee FOR INSERT TO anon WITH CHECK (true);
CREATE POLICY "Allow anon update on coffee" ON coffee FOR UPDATE TO anon USING (true);

CREATE POLICY "Allow anon crud on favorites" ON favorites FOR ALL TO anon USING (true) WITH CHECK (true);
CREATE POLICY "Allow anon crud on cart" ON cart FOR ALL TO anon USING (true) WITH CHECK (true);
CREATE POLICY "Allow anon crud on addresses" ON addresses FOR ALL TO anon USING (true) WITH CHECK (true);
CREATE POLICY "Allow anon crud on orders" ON orders FOR ALL TO anon USING (true) WITH CHECK (true);
CREATE POLICY "Allow anon crud on order_items" ON order_items FOR ALL TO anon USING (true) WITH CHECK (true);
CREATE POLICY "Allow anon crud on search_history" ON search_history FOR ALL TO anon USING (true) WITH CHECK (true);

-- Functions & Triggers
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
