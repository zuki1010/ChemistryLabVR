ALTER TABLE public."Reaction"
    ADD COLUMN conditions_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    ADD COLUMN is_reversible BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN source_reference TEXT;

CREATE TABLE public."ReactionComponent" (
                                            id BIGSERIAL PRIMARY KEY,
                                            reaction_id BIGINT NOT NULL,
                                            chemical_id BIGINT NOT NULL,
                                            component_type VARCHAR(20) NOT NULL,
                                            coefficient INTEGER NOT NULL,
                                            display_order INTEGER NOT NULL DEFAULT 1,
                                            phase_change VARCHAR(20) NOT NULL DEFAULT 'NONE',

                                            CONSTRAINT fk_reaction_component_reaction
                                                FOREIGN KEY (reaction_id)
                                                    REFERENCES public."Reaction"(id)
                                                    ON DELETE CASCADE,

                                            CONSTRAINT fk_reaction_component_chemical
                                                FOREIGN KEY (chemical_id)
                                                    REFERENCES public."Chemical"(id)
                                                    ON DELETE RESTRICT,

                                            CONSTRAINT uq_reaction_component_chemical_type
                                                UNIQUE (reaction_id, component_type, chemical_id),

                                            CONSTRAINT ck_reaction_component_type
                                                CHECK (component_type IN ('REACTANT', 'PRODUCT', 'CATALYST')),

                                            CONSTRAINT ck_reaction_component_coefficient
                                                CHECK (coefficient > 0),

                                            CONSTRAINT ck_reaction_component_order
                                                CHECK (display_order > 0),

                                            CONSTRAINT ck_reaction_component_phase
                                                CHECK (phase_change IN ('NONE', 'GAS', 'PRECIPITATE')),

                                            CONSTRAINT ck_catalyst_phase
                                                CHECK (component_type <> 'CATALYST' OR phase_change = 'NONE')
);

CREATE INDEX ix_reaction_component_chemical
    ON public."ReactionComponent"(chemical_id);