--
-- PostgreSQL database dump
--

-- Dumped from database version 18.4
-- Dumped by pg_dump version 18.4

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
-- Name: add_game(text, integer, text, text, text, text, text, text[], text, integer, boolean, text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.add_game(p_title text, p_release_year integer, p_developer text, p_publisher text, p_platform text, p_region text DEFAULT 'PAL'::text, p_format text DEFAULT NULL::text, p_genres text[] DEFAULT '{}'::text[], p_synopsis text DEFAULT NULL::text, p_platform_year integer DEFAULT NULL::integer, p_owned boolean DEFAULT true, p_port_developer text DEFAULT NULL::text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_game_id     int;
    v_platform_id int;
    v_unknown     text[];
BEGIN
    SELECT id INTO v_platform_id
    FROM platforms
    WHERE abbreviation = p_platform OR name = p_platform;

    IF v_platform_id IS NULL THEN
        RAISE EXCEPTION 'Unknown platform: %', p_platform
            USING HINT = 'Check platforms.abbreviation (PS1, PS2, GBC, GBA, NDS, 3DS, NSW...)';
    END IF;

    SELECT ARRAY_AGG(x) INTO v_unknown
    FROM UNNEST(p_genres) AS x
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = x);

    IF v_unknown IS NOT NULL THEN
        RAISE EXCEPTION 'Unknown genre(s): %', ARRAY_TO_STRING(v_unknown, ', ')
            USING HINT = 'Insert them into genres first, or fix the spelling (accents count).';
    END IF;

    SELECT id INTO v_game_id
    FROM games
    WHERE lower(title) = lower(p_title)
      AND release_year IS NOT DISTINCT FROM p_release_year;

    IF v_game_id IS NULL THEN
        INSERT INTO games (title, release_year, developer, publisher, synopsis)
        VALUES (p_title, p_release_year, p_developer, p_publisher, p_synopsis)
        RETURNING id INTO v_game_id;
    END IF;

    INSERT INTO editions (game_id, platform_id, release_year, region, format, owned, port_developer)
    VALUES (v_game_id, v_platform_id,
            COALESCE(p_platform_year, p_release_year),
            p_region, p_format, p_owned, p_port_developer)
    ON CONFLICT ON CONSTRAINT uq_editions DO NOTHING;

    INSERT INTO game_genres (game_id, genre_id)
    SELECT v_game_id, g.id
    FROM genres g
    WHERE g.name = ANY(p_genres)
    ON CONFLICT DO NOTHING;

    RETURN v_game_id;
END;
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: editions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.editions (
    id integer NOT NULL,
    game_id integer NOT NULL,
    platform_id integer NOT NULL,
    release_year smallint,
    region character varying(10),
    format character varying(20),
    owned boolean DEFAULT false NOT NULL,
    notes text,
    port_developer character varying(120),
    CONSTRAINT editions_format_check CHECK (((format)::text = ANY ((ARRAY['cartucho'::character varying, 'CD'::character varying, 'DVD'::character varying, 'Blu-ray'::character varying, 'BR'::character varying, 'BD'::character varying, 'tarjeta'::character varying, 'digital'::character varying])::text[]))),
    CONSTRAINT editions_region_check CHECK (((region)::text = ANY ((ARRAY['PAL'::character varying, 'NTSC-U'::character varying, 'NTSC-J'::character varying])::text[]))),
    CONSTRAINT editions_release_year_check CHECK (((release_year >= 1970) AND (release_year <= 2100)))
);


--
-- Name: TABLE editions; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.editions IS 'A game published on a specific platform. The core of the schema.';


--
-- Name: COLUMN editions.owned; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.editions.owned IS 'FALSE = catalogued but not in the collection (wishlist). TRUE = physically owned. Lives here and not on games because ownership depends on version.';


--
-- Name: COLUMN editions.port_developer; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.editions.port_developer IS 'Desarrollador que realizó la conversión a esta plataforma, si es diferente del desarrollador del juego. NULL si es el mismo.';


--
-- Name: editions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.editions ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.editions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: game_genres; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.game_genres (
    game_id integer NOT NULL,
    genre_id integer NOT NULL
);


--
-- Name: games; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.games (
    id integer NOT NULL,
    title character varying(200) NOT NULL,
    release_year smallint,
    developer character varying(120),
    publisher character varying(120),
    synopsis text,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    notes text,
    edition_type character varying(20) DEFAULT 'original'::character varying,
    CONSTRAINT games_edition_type_check CHECK (((edition_type)::text = ANY ((ARRAY['original'::character varying, 'remake'::character varying, 'remaster'::character varying, 'port'::character varying])::text[]))),
    CONSTRAINT games_release_year_check CHECK (((release_year >= 1950) AND (release_year <= 2100)))
);


--
-- Name: COLUMN games.release_year; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.games.release_year IS 'Year of the first worldwide release, regardless of platform. Not the same as editions.release_year.';


--
-- Name: COLUMN games.created_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.games.created_at IS 'Audit column: when the row entered this database, NOT a fact about the game. Filled automatically by the DEFAULT.';


--
-- Name: games_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.games ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.games_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: genres; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.genres (
    id integer NOT NULL,
    name character varying(60) NOT NULL
);


--
-- Name: genres_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.genres ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.genres_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: platforms; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.platforms (
    id integer NOT NULL,
    name character varying(60) NOT NULL,
    abbreviation character varying(10) NOT NULL,
    manufacturer character varying(60) NOT NULL,
    release_year smallint,
    CONSTRAINT platforms_release_year_check CHECK (((release_year >= 1950) AND (release_year <= 2100)))
);


--
-- Name: TABLE platforms; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.platforms IS 'Consoles and systems.';


--
-- Name: COLUMN platforms.abbreviation; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.platforms.abbreviation IS 'Short code used in the UI and in add_game(): PS1, GBA, NSW...';


--
-- Name: platforms_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.platforms ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.platforms_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

--
-- Name: v_catalog; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_catalog AS
SELECT
    NULL::integer AS edition_id,
    NULL::integer AS game_id,
    NULL::character varying(200) AS title,
    NULL::smallint AS original_year,
    NULL::smallint AS platform_year,
    NULL::character varying(120) AS developer,
    NULL::character varying(120) AS publisher,
    NULL::text AS synopsis,
    NULL::character varying(60) AS platform,
    NULL::character varying(10) AS abbreviation,
    NULL::character varying(60) AS manufacturer,
    NULL::character varying(10) AS region,
    NULL::character varying(20) AS format,
    NULL::boolean AS owned,
    NULL::text AS notes,
    NULL::timestamp with time zone AS created_at,
    NULL::character varying[] AS genres;


--
-- Name: v_games; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_games AS
SELECT
    NULL::integer AS id,
    NULL::character varying(200) AS title,
    NULL::smallint AS release_year,
    NULL::character varying(120) AS developer,
    NULL::character varying(120) AS publisher,
    NULL::text AS synopsis,
    NULL::timestamp with time zone AS created_at,
    NULL::character varying[] AS platforms,
    NULL::character varying[] AS genres,
    NULL::bigint AS edition_count,
    NULL::boolean AS owned_anywhere;


--
-- Name: v_platform_stats; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_platform_stats AS
SELECT
    NULL::integer AS platform_id,
    NULL::character varying(60) AS platform,
    NULL::character varying(10) AS abbreviation,
    NULL::character varying(60) AS manufacturer,
    NULL::smallint AS release_year,
    NULL::bigint AS catalogued,
    NULL::bigint AS owned,
    NULL::bigint AS wishlist;


--
-- Data for Name: editions; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.editions (id, game_id, platform_id, release_year, region, format, owned, notes, port_developer) FROM stdin;
1	1	1	1999	PAL	CD	t	\N	\N
2	2	1	1999	PAL	CD	t	\N	\N
3	3	1	2000	PAL	CD	t	\N	\N
4	4	1	1998	PAL	CD	t	\N	Krisalis Software
5	5	1	1998	PAL	CD	t	\N	\N
6	6	1	1999	PAL	CD	t	\N	\N
7	7	1	1995	PAL	CD	t	\N	\N
8	8	1	1996	PAL	CD	t	\N	\N
9	9	1	1996	PAL	CD	t	\N	\N
10	10	2	2005	PAL	DVD	t	\N	\N
11	11	2	2005	PAL	DVD	t	\N	\N
12	12	2	2007	PAL	DVD	t	\N	\N
14	14	2	2004	PAL	DVD	t	\N	\N
13	13	2	2007	PAL	DVD	t	Publicado bajo el sello RedOctane, filial de Activision	\N
16	15	2	2004	PAL	DVD	t	\N	\N
17	16	3	2007	PAL	BD	t	Desarrollado por Ubisoft Montreal	\N
18	17	3	2010	PAL	BD	t	\N	\N
19	18	3	2010	PAL	BD	t	\N	\N
20	19	4	2019	PAL	BD	t	\N	\N
\.


--
-- Data for Name: game_genres; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.game_genres (game_id, genre_id) FROM stdin;
1	14
2	22
3	24
3	31
3	32
4	24
4	31
4	32
5	3
6	3
6	18
7	8
8	8
9	3
9	20
10	14
11	1
12	22
13	25
14	3
14	13
14	26
15	22
16	3
16	18
16	26
17	1
17	6
17	26
18	3
18	33
19	13
19	19
\.


--
-- Data for Name: games; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.games (id, title, release_year, developer, publisher, synopsis, created_at, notes, edition_type) FROM stdin;
1	Medal of Honor	1999	DreamWorks Interactive	Electronic Arts	Tomando el papel del teniente Jimmy Patterson, un piloto de C-47 reclutado por la Oficina de Servicios Estratégicos (OSS), el jugador realiza diversas misiones ambientadas en los compases finales de la Segunda Guerra Mundial (1944-1945). El juego fue ideado por el director Steven Spielberg	2026-07-31 00:34:28.180888+02	\N	original
2	Gran Turismo 2	1999	Polyphony Digital	Sony Computer Entertainment	\N	2026-08-02 19:09:47.008958+02	\N	original
3	Theme Park World	1999	Bullfrog Productions	Electronic Arts	El jugador asume el papel de un magnate diseñador y constructor de parques de atracciones temáticos	2026-08-04 14:28:36.427864+02	\N	original
4	Theme Hospital	1997	Bullfrog Productions	Electronic Arts	El jugador dirige un hospital privado, y deberá construir instalaciones, gestionar recursos y curar enfermedades absurdas	2026-08-04 14:42:22.779123+02	\N	original
5	MediEvil	1998	Sony Computer Entertainment	Sony Computer Entertainment	Sir Daniel Fortesque, un caballero cobarde resucitado por error como esqueleto, debe derrotar al brujo Zarok para salvar el reino de Gallowmere y convertirse en un verdadero héroe.	2026-08-05 12:45:30.550864+02	\N	original
6	Metal Gear Solid	1998	Konami	Konami	El ex-soldado Solid Snake se infiltra en la base militar Shadow Moses, controlada por los terroristas de FOXHOUND, para neutralizar un ataque nuclear.	2026-08-05 13:04:01.110212+02	\N	original
7	Rayman	1995	Ubisoft	Ubisoft	Rayman debe rescatar a los Electoons cautivos y derrotar al malvado Mr. Dark en varios niveles muy coloridos para devolver la paz a su mundo.	2026-08-05 13:12:49.792542+02	\N	original
8	Crash Bandicoot	1996	Naughty Dog	Sony Computer Entertainment	Crash Bandicoot es un marsupial genéticamente modificado que debe atravesar peligrosos mundos para detener los planes de su malvado creador, el Dr. Neo Cortex.	2026-08-05 13:22:49.101576+02	\N	original
9	Resident Evil	1996	Capcom	Capcom	El equipo Alfa de S.T.A.R.S. queda atrapado en una misteriosa mansión llena de zombies tras investigar extraños asesinatos, descubriendo que es un laboratorio secreto de la Corporación Umbrella.	2026-08-05 13:34:56.62019+02	\N	original
10	Call of Duty 2: Big Red One	2005	Treyarch	Activision	El jugador forma parte de la 1.ª División de Infantería de EE. UU. (la «Big Red One») y su lucha en batallas clave de la Segunda Guerra Mundial.	2026-08-05 18:02:57.26667+02	\N	original
11	Star Wars: Episodio III - La Venganza de los Sith	2005	The Collective	LucasArts	Toma el papel de Anakin Skywalker y de Obi-Wan Kenobi y participa en duelos épicos de la película con el sable de luz o usando la Fuerza.	2026-08-05 18:14:37.454764+02	\N	original
12	Need for Speed: ProStreet	2007	EA Black Box	Electronic Arts	Eres Ryan Cooper, un antiguo corredor callejero ilegal que decide reformarse y llevar sus habilidades a circuitos profesionales cerrados y legales.	2026-08-05 18:18:28.823277+02	\N	original
13	Guitar Hero: Rocks the 80s	2007	Harmonix Music Systems	Activision	Toma la guitarra (o el mando) y toca las mejores canciones de los '80 en este videojuego musical	2026-08-05 18:27:02.754756+02	\N	original
14	Gran Theft Auto: San Andreas	2004	Rockstar Games	Rockstar Games	Carl "CJ" Johnson vuelve a su Los Santos natal tras el asesinato de su madre, donde es incriminado injustamente por policías corruptos y debe recorrer todo un estado para limpiar su nombre, salvar a su familia y recuperar el control de su banda	2026-08-05 18:32:39.474778+02	\N	original
15	Formula One 04	2004	Studio Liverpool	Sony Computer Entertainment	Videojuego oficial de carreras para PlayStation 2 que simula la temporada del campeonato mundial de Fórmula 1 del año 2004	2026-08-05 18:36:36.986994+02	\N	original
16	Assassin's Creed	2007	Ubisoft	Ubisoft	Eres Desmond Miles, secuestrado por la corporación Abstergo para obligarte a revivir, mediante la máquina Animus, los recuerdos de tu antepasado Altaïr, un Maestro Asesino que lucha contra los Templarios en Tierra Santa durante la Tercera Cruzada para recuperar un artefacto místico	2026-08-05 18:46:55.57225+02	\N	original
17	Fallout: New Vegas	2010	Obsidian Entertainment	Namco Bandai	Lucha por abrirte paso por el tórrido desierto de Mojave hasta New Vegas, y toma partido en la guerra por el control de esta ciudad y la presa Hoover.	2026-08-05 18:53:33.917627+02	\N	original
18	Heavy Rain	2010	Quantic Dream	Sony Computer Entertainment	El asesino del origami ha desatado el pánico en la ciudad. Controla a los cuatro protagonistas de esta intriga psicológica donde cada decisión puede tener consecuencias.	2026-08-05 19:00:12.42284+02	\N	original
19	Resident Evil 2 (2019)	2019	Capcom	Capcom	Toma el papel del policía novato Leon S. Kennedy y la estudiante universitaria Claire Redfield. Atrapados en Raccoon City en una epidemia zombie provocada por el virus de la corporación Umbrella, deben unirse para escapar.	2026-08-05 19:09:41.585698+02	\N	original
\.


--
-- Data for Name: genres; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.genres (id, name) FROM stdin;
1	Acción
2	Aventura
3	Acción-aventura
4	Aventura gráfica
5	Rol
6	Rol de acción
7	Rol táctico
8	Plataformas
9	Metroidvania
10	Puzle
11	Estrategia
12	Estrategia en tiempo real
13	Disparos
14	Shooter en primera persona
15	Matamarcianos
16	Lucha
17	Beat 'em up
18	Sigilo
19	Survival horror
20	Terror
21	Conducción
22	Carreras
23	Deportes
24	Simulación
25	Musical
26	Mundo abierto
27	Sandbox
28	Novela visual
29	Party game
30	Arcade
31	Construcción
32	Gestión
33	Drama interactivo
\.


--
-- Data for Name: platforms; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.platforms (id, name, abbreviation, manufacturer, release_year) FROM stdin;
1	PlayStation	PS1	Sony	1994
2	PlayStation 2	PS2	Sony	2000
3	PlayStation 3	PS3	Sony	2006
4	PlayStation 4	PS4	Sony	2013
5	Game Boy Color	GBC	Nintendo	1998
6	Game Boy Advance	GBA	Nintendo	2001
7	Nintendo DS	NDS	Nintendo	2004
8	Nintendo 3DS	3DS	Nintendo	2011
9	Nintendo Switch	NSW	Nintendo	2017
\.


--
-- Name: editions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.editions_id_seq', 20, true);


--
-- Name: games_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.games_id_seq', 19, true);


--
-- Name: genres_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.genres_id_seq', 33, true);


--
-- Name: platforms_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.platforms_id_seq', 9, true);


--
-- Name: editions editions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.editions
    ADD CONSTRAINT editions_pkey PRIMARY KEY (id);


--
-- Name: game_genres game_genres_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.game_genres
    ADD CONSTRAINT game_genres_pkey PRIMARY KEY (game_id, genre_id);


--
-- Name: games games_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.games
    ADD CONSTRAINT games_pkey PRIMARY KEY (id);


--
-- Name: genres genres_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.genres
    ADD CONSTRAINT genres_name_key UNIQUE (name);


--
-- Name: genres genres_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.genres
    ADD CONSTRAINT genres_pkey PRIMARY KEY (id);


--
-- Name: platforms platforms_abbreviation_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.platforms
    ADD CONSTRAINT platforms_abbreviation_key UNIQUE (abbreviation);


--
-- Name: platforms platforms_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.platforms
    ADD CONSTRAINT platforms_name_key UNIQUE (name);


--
-- Name: platforms platforms_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.platforms
    ADD CONSTRAINT platforms_pkey PRIMARY KEY (id);


--
-- Name: editions uq_editions; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.editions
    ADD CONSTRAINT uq_editions UNIQUE (game_id, platform_id, region);


--
-- Name: idx_editions_game; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_editions_game ON public.editions USING btree (game_id);


--
-- Name: idx_editions_owned; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_editions_owned ON public.editions USING btree (owned) WHERE owned;


--
-- Name: idx_editions_platform; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_editions_platform ON public.editions USING btree (platform_id);


--
-- Name: idx_game_genres_genre; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_game_genres_genre ON public.game_genres USING btree (genre_id);


--
-- Name: idx_games_title_lower; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_games_title_lower ON public.games USING btree (lower((title)::text));


--
-- Name: idx_games_year; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_games_year ON public.games USING btree (release_year);


--
-- Name: v_catalog _RETURN; Type: RULE; Schema: public; Owner: -
--

CREATE OR REPLACE VIEW public.v_catalog AS
 SELECT e.id AS edition_id,
    g.id AS game_id,
    g.title,
    g.release_year AS original_year,
    e.release_year AS platform_year,
    g.developer,
    g.publisher,
    g.synopsis,
    p.name AS platform,
    p.abbreviation,
    p.manufacturer,
    e.region,
    e.format,
    e.owned,
    e.notes,
    g.created_at,
    array_remove(array_agg(DISTINCT gn.name), NULL::character varying) AS genres
   FROM ((((public.editions e
     JOIN public.games g ON ((g.id = e.game_id)))
     JOIN public.platforms p ON ((p.id = e.platform_id)))
     LEFT JOIN public.game_genres gg ON ((gg.game_id = g.id)))
     LEFT JOIN public.genres gn ON ((gn.id = gg.genre_id)))
  GROUP BY e.id, g.id, p.id;


--
-- Name: v_games _RETURN; Type: RULE; Schema: public; Owner: -
--

CREATE OR REPLACE VIEW public.v_games AS
 SELECT g.id,
    g.title,
    g.release_year,
    g.developer,
    g.publisher,
    g.synopsis,
    g.created_at,
    array_remove(array_agg(DISTINCT p.abbreviation), NULL::character varying) AS platforms,
    array_remove(array_agg(DISTINCT gn.name), NULL::character varying) AS genres,
    count(DISTINCT e.id) AS edition_count,
    COALESCE(bool_or(e.owned), false) AS owned_anywhere
   FROM ((((public.games g
     LEFT JOIN public.editions e ON ((e.game_id = g.id)))
     LEFT JOIN public.platforms p ON ((p.id = e.platform_id)))
     LEFT JOIN public.game_genres gg ON ((gg.game_id = g.id)))
     LEFT JOIN public.genres gn ON ((gn.id = gg.genre_id)))
  GROUP BY g.id;


--
-- Name: v_platform_stats _RETURN; Type: RULE; Schema: public; Owner: -
--

CREATE OR REPLACE VIEW public.v_platform_stats AS
 SELECT p.id AS platform_id,
    p.name AS platform,
    p.abbreviation,
    p.manufacturer,
    p.release_year,
    count(e.id) AS catalogued,
    count(e.id) FILTER (WHERE e.owned) AS owned,
    count(e.id) FILTER (WHERE (NOT e.owned)) AS wishlist
   FROM (public.platforms p
     LEFT JOIN public.editions e ON ((e.platform_id = p.id)))
  GROUP BY p.id;


--
-- Name: editions editions_game_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.editions
    ADD CONSTRAINT editions_game_id_fkey FOREIGN KEY (game_id) REFERENCES public.games(id) ON DELETE CASCADE;


--
-- Name: editions editions_platform_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.editions
    ADD CONSTRAINT editions_platform_id_fkey FOREIGN KEY (platform_id) REFERENCES public.platforms(id) ON DELETE RESTRICT;


--
-- Name: game_genres game_genres_game_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.game_genres
    ADD CONSTRAINT game_genres_game_id_fkey FOREIGN KEY (game_id) REFERENCES public.games(id) ON DELETE CASCADE;


--
-- Name: game_genres game_genres_genre_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.game_genres
    ADD CONSTRAINT game_genres_genre_id_fkey FOREIGN KEY (genre_id) REFERENCES public.genres(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

