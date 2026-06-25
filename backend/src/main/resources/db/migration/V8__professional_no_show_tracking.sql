-- The ProfessionalProfile entity gained `noShowCount` and `suspended` fields (no-show
-- accountability) but no migration ever created the matching columns, so any query that
-- selects from professional_profiles (e.g. the directory search) failed at runtime.
ALTER TABLE professional_profiles ADD COLUMN IF NOT EXISTS no_show_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE professional_profiles ADD COLUMN IF NOT EXISTS suspended BOOLEAN NOT NULL DEFAULT false;
