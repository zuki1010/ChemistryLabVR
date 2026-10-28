--
-- PostgreSQL database dump
--

-- Dumped from database version 18.6
-- Dumped by pg_dump version 18.6

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: -
--

-- *not* creating schema, since initdb creates it


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: Assignment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."Assignment" (
                                     id bigint NOT NULL,
                                     category_id bigint NOT NULL,
                                     title text NOT NULL,
                                     type text NOT NULL,
                                     description text NOT NULL,
                                     target_reaction_id bigint,
                                     max_score integer NOT NULL,
                                     created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: AssignmentChemical; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."AssignmentChemical" (
                                             id bigint NOT NULL,
                                             assignment_id bigint NOT NULL,
                                             chemical_id bigint NOT NULL,
                                             allowed_quantity numeric(10,3) NOT NULL
);


--
-- Name: AssignmentChemical_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public."AssignmentChemical_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AssignmentChemical_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public."AssignmentChemical_id_seq" OWNED BY public."AssignmentChemical".id;


--
-- Name: Assignment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public."Assignment_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: Assignment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public."Assignment_id_seq" OWNED BY public."Assignment".id;


--
-- Name: Category; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."Category" (
                                   id bigint NOT NULL,
                                   name text NOT NULL,
                                   description text NOT NULL,
                                   created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: Category_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public."Category_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: Category_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public."Category_id_seq" OWNED BY public."Category".id;


--
-- Name: Chemical; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."Chemical" (
                                   id bigint NOT NULL,
                                   name text NOT NULL,
                                   formula text NOT NULL,
                                   state text NOT NULL,
                                   color_hex text NOT NULL,
                                   description text,
                                   unity_asset_id text NOT NULL,
                                   created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                   density numeric(10,3),
                                   danger_level integer,
                                   unit text
);


--
-- Name: Chemical_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public."Chemical_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: Chemical_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public."Chemical_id_seq" OWNED BY public."Chemical".id;


--
-- Name: Reaction; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."Reaction" (
                                   id bigint NOT NULL,
                                   equation text NOT NULL,
                                   reactants_json text NOT NULL,
                                   products_json text NOT NULL,
                                   visual_effects_json text NOT NULL,
                                   description text NOT NULL,
                                   created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: Reaction_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public."Reaction_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: Reaction_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public."Reaction_id_seq" OWNED BY public."Reaction".id;


--
-- Name: RevokedAccessTokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."RevokedAccessTokens" (
                                              jti uuid NOT NULL,
                                              user_id uuid,
                                              expires_at timestamp with time zone NOT NULL,
                                              revoked_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: Submission; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."Submission" (
                                     id bigint NOT NULL,
                                     user_id uuid NOT NULL,
                                     assignment_id bigint NOT NULL,
                                     score numeric(5,2) NOT NULL,
                                     execution_time_seconds integer NOT NULL,
                                     action_logs_json text NOT NULL,
                                     submitted_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: Submission_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public."Submission_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: Submission_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public."Submission_id_seq" OWNED BY public."Submission".id;


--
-- Name: User; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."User" (
                               id uuid DEFAULT gen_random_uuid() NOT NULL,
                               email text NOT NULL,
                               password text NOT NULL,
                               "fullName" text NOT NULL,
                               role text NOT NULL,
                               is_active boolean DEFAULT true NOT NULL,
                               created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                               updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: UserLoginLogs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."UserLoginLogs" (
                                        id text NOT NULL,
                                        user_id uuid,
                                        email_attempted text,
                                        status text,
                                        ip_address text,
                                        user_agent text,
                                        failure_reason text,
                                        login_at timestamp with time zone
);


--
-- Name: UserSession; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."UserSession" (
                                      id bigint NOT NULL,
                                      user_id uuid NOT NULL,
                                      token text NOT NULL,
                                      expired_at timestamp with time zone NOT NULL,
                                      is_revoked boolean DEFAULT false NOT NULL,
                                      created_at timestamp with time zone NOT NULL
);


--
-- Name: UserSession_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public."UserSession_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: UserSession_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public."UserSession_id_seq" OWNED BY public."UserSession".id;


--
-- Name: Assignment id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Assignment" ALTER COLUMN id SET DEFAULT nextval('public."Assignment_id_seq"'::regclass);


--
-- Name: AssignmentChemical id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."AssignmentChemical" ALTER COLUMN id SET DEFAULT nextval('public."AssignmentChemical_id_seq"'::regclass);


--
-- Name: Category id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Category" ALTER COLUMN id SET DEFAULT nextval('public."Category_id_seq"'::regclass);


--
-- Name: Chemical id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Chemical" ALTER COLUMN id SET DEFAULT nextval('public."Chemical_id_seq"'::regclass);


--
-- Name: Reaction id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Reaction" ALTER COLUMN id SET DEFAULT nextval('public."Reaction_id_seq"'::regclass);


--
-- Name: Submission id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Submission" ALTER COLUMN id SET DEFAULT nextval('public."Submission_id_seq"'::regclass);


--
-- Name: UserSession id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserSession" ALTER COLUMN id SET DEFAULT nextval('public."UserSession_id_seq"'::regclass);


--
-- Name: AssignmentChemical AssignmentChemical_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."AssignmentChemical"
    ADD CONSTRAINT "AssignmentChemical_pkey" PRIMARY KEY (id);


--
-- Name: Assignment Assignment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Assignment"
    ADD CONSTRAINT "Assignment_pkey" PRIMARY KEY (id);


--
-- Name: Category Category_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Category"
    ADD CONSTRAINT "Category_pkey" PRIMARY KEY (id);


--
-- Name: Chemical Chemical_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Chemical"
    ADD CONSTRAINT "Chemical_pkey" PRIMARY KEY (id);


--
-- Name: Reaction Reaction_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Reaction"
    ADD CONSTRAINT "Reaction_pkey" PRIMARY KEY (id);


--
-- Name: RevokedAccessTokens RevokedAccessTokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."RevokedAccessTokens"
    ADD CONSTRAINT "RevokedAccessTokens_pkey" PRIMARY KEY (jti);


--
-- Name: Submission Submission_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Submission"
    ADD CONSTRAINT "Submission_pkey" PRIMARY KEY (id);


--
-- Name: UserLoginLogs UserLoginLogs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserLoginLogs"
    ADD CONSTRAINT "UserLoginLogs_pkey" PRIMARY KEY (id);


--
-- Name: UserSession UserSession_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserSession"
    ADD CONSTRAINT "UserSession_pkey" PRIMARY KEY (id);


--
-- Name: User User_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."User"
    ADD CONSTRAINT "User_pkey" PRIMARY KEY (id);


--
-- Name: Chemical formula; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Chemical"
    ADD CONSTRAINT formula UNIQUE (formula);


--
-- Name: Chemical name; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Chemical"
    ADD CONSTRAINT name UNIQUE (name);


--
-- Name: UserSession token; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserSession"
    ADD CONSTRAINT token UNIQUE (token);


--
-- Name: Chemical unity_asset_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Chemical"
    ADD CONSTRAINT unity_asset_id UNIQUE (unity_asset_id);


--
-- Name: AssignmentChemical AssignmentChemical_assignment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."AssignmentChemical"
    ADD CONSTRAINT "AssignmentChemical_assignment_id_fkey" FOREIGN KEY (assignment_id) REFERENCES public."Assignment"(id) ON DELETE CASCADE;


--
-- Name: AssignmentChemical AssignmentChemical_chemical_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."AssignmentChemical"
    ADD CONSTRAINT "AssignmentChemical_chemical_id_fkey" FOREIGN KEY (chemical_id) REFERENCES public."Chemical"(id) ON DELETE CASCADE;


--
-- Name: Assignment Assignment_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Assignment"
    ADD CONSTRAINT "Assignment_category_id_fkey" FOREIGN KEY (category_id) REFERENCES public."Category"(id) ON DELETE CASCADE;


--
-- Name: Assignment Assignment_target_reaction_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Assignment"
    ADD CONSTRAINT "Assignment_target_reaction_id_fkey" FOREIGN KEY (target_reaction_id) REFERENCES public."Reaction"(id) ON DELETE SET NULL;


--
-- Name: Submission Submission_assignment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Submission"
    ADD CONSTRAINT "Submission_assignment_id_fkey" FOREIGN KEY (assignment_id) REFERENCES public."Assignment"(id) ON DELETE CASCADE;


--
-- Name: Submission Submission_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Submission"
    ADD CONSTRAINT "Submission_user_id_fkey" FOREIGN KEY (user_id) REFERENCES public."User"(id) ON DELETE CASCADE;


--
-- Name: RevokedAccessTokens user_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."RevokedAccessTokens"
    ADD CONSTRAINT user_id FOREIGN KEY (user_id) REFERENCES public."User"(id) ON DELETE CASCADE;


--
-- Name: UserLoginLogs user_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserLoginLogs"
    ADD CONSTRAINT user_id FOREIGN KEY (user_id) REFERENCES public."User"(id) ON DELETE SET NULL;


--
-- Name: UserSession user_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserSession"
    ADD CONSTRAINT user_id FOREIGN KEY (user_id) REFERENCES public."User"(id) ON DELETE CASCADE NOT VALID;


--
-- PostgreSQL database dump complete
--

