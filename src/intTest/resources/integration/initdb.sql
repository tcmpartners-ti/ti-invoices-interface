--
-- PostgreSQL database dump
--

-- Dumped from database version 16.1
-- Dumped by pg_dump version 16.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: account; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.account (
    acc_type character varying(10),
    cus_mnm character varying(20),
    ext_acctno character varying(34),
    acct_key character varying(255) NOT NULL
);


ALTER TABLE public.account OWNER TO test;

--
-- Name: extevent; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.extevent (
    key29 bigint NOT NULL,
    finseacc character varying(255)
);


ALTER TABLE public.extevent OWNER TO test;

--
-- Name: extmaster; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.extmaster (
    key29 bigint NOT NULL,
    master bigint NOT NULL,
    finacc character varying(255)
);


ALTER TABLE public.extmaster OWNER TO test;

--
-- Name: extprogramme; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.extprogramme (
    exfinday integer,
    exfinreq character varying(255),
    pid character varying(255) NOT NULL,
    CONSTRAINT extprogramme_exfinreq_check CHECK (((exfinreq)::text = ANY ((ARRAY['N'::character varying, 'Y'::character varying])::text[])))
);


ALTER TABLE public.extprogramme OWNER TO test;

--
-- Name: gfpf; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.gfpf (
    date_dl numeric(7,0),
    gfca2 character varying(2),
    gfclc character varying(3),
    gfcnal character varying(2),
    gfcnap character varying(2),
    gfcnar character varying(2),
    gfctp character varying(2),
    gfdlm numeric(7,0),
    gflnm character varying(2),
    gfpclc character varying(3),
    source character(1),
    tifcmf character(1),
    gfcus character varying(6),
    gfgrp character varying(6),
    gfpcus character varying(6),
    gfbrnm character varying(8),
    gfctp1 character varying(8),
    gfcus1_sbb character varying(8) NOT NULL,
    gfmtb character varying(8),
    gfaco character varying(10),
    gfc101 character varying(10),
    gfc102 character varying(10),
    gfcpnc character varying(12),
    gfdas character varying(15),
    gfc201 character varying(20),
    gfc202 character varying(20),
    gfcrf character varying(20),
    gfcus1 character varying(20) NOT NULL,
    gfpcus1 character varying(20),
    clr_number character varying(34),
    gfcun character varying(35),
    ticustloc character varying(60),
    gfcub character varying(255),
    gfcuc character varying(255),
    gfcud character varying(255),
    gfcuz character varying(255),
    mnt_in_bo character varying(255),
    prime4swft character varying(255),
    CONSTRAINT gfpf_gfcub_check CHECK (((gfcub)::text = ANY ((ARRAY['N'::character varying, 'Y'::character varying])::text[]))),
    CONSTRAINT gfpf_gfcuc_check CHECK (((gfcuc)::text = ANY ((ARRAY['N'::character varying, 'Y'::character varying])::text[]))),
    CONSTRAINT gfpf_gfcud_check CHECK (((gfcud)::text = ANY ((ARRAY['N'::character varying, 'Y'::character varying])::text[]))),
    CONSTRAINT gfpf_gfcuz_check CHECK (((gfcuz)::text = ANY ((ARRAY['N'::character varying, 'Y'::character varying])::text[]))),
    CONSTRAINT gfpf_mnt_in_bo_check CHECK (((mnt_in_bo)::text = ANY ((ARRAY['N'::character varying, 'Y'::character varying])::text[]))),
    CONSTRAINT gfpf_prime4swft_check CHECK (((prime4swft)::text = ANY ((ARRAY['N'::character varying, 'Y'::character varying])::text[])))
);


ALTER TABLE public.gfpf OWNER TO test;

--
-- Name: invmaster; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.invmaster (
    adj_amt numeric(15,0),
    adj_ccy character varying(3),
    adj_dir character varying(4),
    avail_amt numeric(15,0),
    avail_ccy character varying(3),
    credn_amt numeric(15,0),
    credn_ccy character varying(3),
    deal_amt numeric(15,0),
    deal_ccy character varying(3),
    disc_amt numeric(15,0),
    disc_ccy character varying(3),
    duedate date,
    elig_dtls character(1),
    equiv_amt numeric(15,0),
    equiv_ccy character varying(3),
    face_amt numeric(15,0),
    face_ccy character varying(3),
    invdatercd date,
    invoicefor character(1),
    outs_amt numeric(15,0),
    outs_ccy character varying(3),
    ovr_elig character(1),
    pfr_ccy character varying(3),
    prog_type character(1),
    status character(1),
    totpayamt numeric(15,0),
    totpayccy character varying(3),
    buyer bigint,
    buyer_pty bigint,
    cust_pty bigint,
    factor_key bigint,
    fince_ev bigint,
    ibpmaster bigint,
    key97 bigint NOT NULL,
    programme bigint,
    seller bigint,
    batchid character varying(20),
    xxxbuyer character varying(20),
    invoic_ref character varying(34),
    vendorcode character varying(35),
    tax_dtls character varying(148),
    gdsdesc character varying(222),
    pay_instr character varying(222),
    sec_dtls character varying(370),
    adv_instr character varying(520),
    cust_notes character varying(520),
    approved character varying(255),
    defer_chg character varying(255),
    disclosed character varying(255),
    eligible character varying(255),
    recourse character varying(255),
    CONSTRAINT invmaster_approved_check CHECK (((approved)::text = ANY ((ARRAY['N'::character varying, 'Y'::character varying])::text[]))),
    CONSTRAINT invmaster_defer_chg_check CHECK (((defer_chg)::text = ANY ((ARRAY['N'::character varying, 'Y'::character varying])::text[]))),
    CONSTRAINT invmaster_disclosed_check CHECK (((disclosed)::text = ANY ((ARRAY['N'::character varying, 'Y'::character varying])::text[]))),
    CONSTRAINT invmaster_eligible_check CHECK (((eligible)::text = ANY ((ARRAY['N'::character varying, 'Y'::character varying])::text[]))),
    CONSTRAINT invmaster_recourse_check CHECK (((recourse)::text = ANY ((ARRAY['N'::character varying, 'Y'::character varying])::text[])))
);


ALTER TABLE public.invmaster OWNER TO test;

--
-- Name: master; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.master (
    ctrct_date date,
    key97 bigint NOT NULL,
    active character varying(255),
    master_ref character varying(255),
    CONSTRAINT master_active_check CHECK (((active)::text = ANY ((ARRAY['N'::character varying, 'Y'::character varying])::text[])))
);


ALTER TABLE public.master OWNER TO test;

--
-- Name: scfcparty; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.scfcparty (
    country character varying(2),
    cpartycnas character varying(2),
    cpartyswb character varying(4),
    cpartyswbr character varying(3),
    cpartyswl character varying(2),
    cpartyxm character varying(2),
    language character varying(2),
    last_maint date,
    limit_amt numeric(15,0),
    limit_ccy character varying(3),
    mnt_in_bo character(1),
    obsolete character(1),
    role character(1),
    status character(1),
    translit character(1),
    tstamp integer,
    typeflag integer,
    branch character varying(8),
    cust_sbb character varying(8),
    key97 bigint NOT NULL,
    programme bigint,
    telex_ans character varying(8),
    zip character varying(15),
    autokey character varying(20),
    cparty character varying(20),
    customer character varying(20),
    fax character varying(20),
    phone character varying(20),
    telex character varying(20),
    cpartyna1 character varying(35),
    cpartyna2 character varying(35),
    cpartyna3 character varying(35),
    cpartyna4 character varying(35),
    cpartyna5 character varying(35),
    cpartyname character varying(35),
    cpartysna1 character varying(35),
    cpartysna2 character varying(35),
    cpartysna3 character varying(35),
    cpartysna4 character varying(35),
    cpartysna5 character varying(35),
    salutation character varying(35),
    email character varying(128),
    cpartynaf oid,
    cpartysnaf oid
);


ALTER TABLE public.scfcparty OWNER TO test;

--
-- Name: scfprogram; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.scfprogram (
    avail_amt numeric(15,0),
    avail_ccy character varying(3),
    createdate date,
    expirydate date,
    fince_dbt character(1),
    fince_to character(1),
    fincereqby character(1),
    last_maint date,
    prog_type character(1),
    startdate date,
    status character(1),
    subtypecat character(1),
    tstamp integer,
    typeflag integer,
    uploadedby character(1),
    bhalf_brn character varying(8),
    cust_sbb character varying(8),
    finprodtyp bigint,
    insurance bigint,
    key97 bigint NOT NULL,
    scfprostyp character varying(10),
    autokey character varying(20),
    customer character varying(20),
    saleref character varying(34),
    id character varying(35),
    descr character varying(60),
    buyaccreq character varying(255),
    mnt_in_bo character varying(255),
    obsolete character varying(255),
    prntguarex character varying(255),
    narrative oid,
    CONSTRAINT scfprogram_buyaccreq_check CHECK (((buyaccreq)::text = ANY ((ARRAY['N'::character varying, 'Y'::character varying])::text[]))),
    CONSTRAINT scfprogram_mnt_in_bo_check CHECK (((mnt_in_bo)::text = ANY ((ARRAY['N'::character varying, 'Y'::character varying])::text[]))),
    CONSTRAINT scfprogram_obsolete_check CHECK (((obsolete)::text = ANY ((ARRAY['N'::character varying, 'Y'::character varying])::text[]))),
    CONSTRAINT scfprogram_prntguarex_check CHECK (((prntguarex)::text = ANY ((ARRAY['N'::character varying, 'Y'::character varying])::text[])))
);


ALTER TABLE public.scfprogram OWNER TO test;

--
-- Name: sx20lf; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.sx20lf (
    addrtype integer NOT NULL,
    sequence integer NOT NULL,
    sxcus1_sbb character varying(8) NOT NULL,
    sxcus1 character varying(20) NOT NULL,
    email character varying(128)
);


ALTER TABLE public.sx20lf OWNER TO test;

--
-- Data for Name: account; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.account (acc_type, cus_mnm, ext_acctno, acct_key) FROM stdin;
\.


--
-- Data for Name: extevent; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.extevent (key29, finseacc) FROM stdin;
\.


--
-- Data for Name: extmaster; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.extmaster (key29, master, finacc) FROM stdin;
\.


--
-- Data for Name: extprogramme; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.extprogramme (exfinday, exfinreq, pid) FROM stdin;
\.


--
-- Data for Name: gfpf; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.gfpf (date_dl, gfca2, gfclc, gfcnal, gfcnap, gfcnar, gfctp, gfdlm, gflnm, gfpclc, source, tifcmf, gfcus, gfgrp, gfpcus, gfbrnm, gfctp1, gfcus1_sbb, gfmtb, gfaco, gfc101, gfc102, gfcpnc, gfdas, gfc201, gfc202, gfcrf, gfcus1, gfpcus1, clr_number, gfcun, ticustloc, gfcub, gfcuc, gfcud, gfcuz, mnt_in_bo, prime4swft) FROM stdin;
\.


--
-- Data for Name: invmaster; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.invmaster (adj_amt, adj_ccy, adj_dir, avail_amt, avail_ccy, credn_amt, credn_ccy, deal_amt, deal_ccy, disc_amt, disc_ccy, duedate, elig_dtls, equiv_amt, equiv_ccy, face_amt, face_ccy, invdatercd, invoicefor, outs_amt, outs_ccy, ovr_elig, pfr_ccy, prog_type, status, totpayamt, totpayccy, buyer, buyer_pty, cust_pty, factor_key, fince_ev, ibpmaster, key97, programme, seller, batchid, xxxbuyer, invoic_ref, vendorcode, tax_dtls, gdsdesc, pay_instr, sec_dtls, adv_instr, cust_notes, approved, defer_chg, disclosed, eligible, recourse) FROM stdin;
\.


--
-- Data for Name: master; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.master (ctrct_date, key97, active, master_ref) FROM stdin;
\.


--
-- Data for Name: scfcparty; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.scfcparty (country, cpartycnas, cpartyswb, cpartyswbr, cpartyswl, cpartyxm, language, last_maint, limit_amt, limit_ccy, mnt_in_bo, obsolete, role, status, translit, tstamp, typeflag, branch, cust_sbb, key97, programme, telex_ans, zip, autokey, cparty, customer, fax, phone, telex, cpartyna1, cpartyna2, cpartyna3, cpartyna4, cpartyna5, cpartyname, cpartysna1, cpartysna2, cpartysna3, cpartysna4, cpartysna5, salutation, email, cpartynaf, cpartysnaf) FROM stdin;
\.


--
-- Data for Name: scfprogram; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.scfprogram (avail_amt, avail_ccy, createdate, expirydate, fince_dbt, fince_to, fincereqby, last_maint, prog_type, startdate, status, subtypecat, tstamp, typeflag, uploadedby, bhalf_brn, cust_sbb, finprodtyp, insurance, key97, scfprostyp, autokey, customer, saleref, id, descr, buyaccreq, mnt_in_bo, obsolete, prntguarex, narrative) FROM stdin;
\.


--
-- Data for Name: sx20lf; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.sx20lf (addrtype, sequence, sxcus1_sbb, sxcus1, email) FROM stdin;
\.


--
-- Name: account account_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.account
    ADD CONSTRAINT account_pkey PRIMARY KEY (acct_key);


--
-- Name: extevent extevent_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.extevent
    ADD CONSTRAINT extevent_pkey PRIMARY KEY (key29);


--
-- Name: extmaster extmaster_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.extmaster
    ADD CONSTRAINT extmaster_pkey PRIMARY KEY (key29);


--
-- Name: extprogramme extprogramme_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.extprogramme
    ADD CONSTRAINT extprogramme_pkey PRIMARY KEY (pid);


--
-- Name: gfpf gfpf_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.gfpf
    ADD CONSTRAINT gfpf_pkey PRIMARY KEY (gfcus1_sbb, gfcus1);


--
-- Name: invmaster invmaster_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.invmaster
    ADD CONSTRAINT invmaster_pkey PRIMARY KEY (key97);


--
-- Name: master master_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.master
    ADD CONSTRAINT master_pkey PRIMARY KEY (key97);


--
-- Name: scfcparty scfcparty_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.scfcparty
    ADD CONSTRAINT scfcparty_pkey PRIMARY KEY (key97);


--
-- Name: scfprogram scfprogram_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.scfprogram
    ADD CONSTRAINT scfprogram_pkey PRIMARY KEY (key97);


--
-- Name: sx20lf sx20lf_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.sx20lf
    ADD CONSTRAINT sx20lf_pkey PRIMARY KEY (addrtype, sequence, sxcus1_sbb, sxcus1);


--
-- Name: sx20lf sx20lf_sxcus1_key; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.sx20lf
    ADD CONSTRAINT sx20lf_sxcus1_key UNIQUE (sxcus1);


--
-- Name: invmaster fk7umubooenawwvj6qdub22l8xa; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.invmaster
    ADD CONSTRAINT fk7umubooenawwvj6qdub22l8xa FOREIGN KEY (seller) REFERENCES public.scfcparty(key97);


--
-- Name: gfpf fkadgv8o8x3h214bv46qgkrrqhg; Type: FK CONSTRAINT; Schema: public; Owner: test
--

--ALTER TABLE ONLY public.gfpf
--    ADD CONSTRAINT fkadgv8o8x3h214bv46qgkrrqhg FOREIGN KEY (gfcus1) REFERENCES public.sx20lf(sxcus1);


--
-- Name: invmaster fkgyl22xxdyi5g343eu3yfgqybv; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.invmaster
    ADD CONSTRAINT fkgyl22xxdyi5g343eu3yfgqybv FOREIGN KEY (buyer) REFERENCES public.scfcparty(key97);


--
-- Name: invmaster fknq7widyanvhq68lm8njqloutw; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.invmaster
    ADD CONSTRAINT fknq7widyanvhq68lm8njqloutw FOREIGN KEY (programme) REFERENCES public.scfprogram(key97);


--
-- PostgreSQL database dump complete
--

-- TEST DATA

INSERT INTO public.gfpf
    (date_dl, gfca2, gfclc, gfcnal, gfcnap, gfcnar, gfctp, gfdlm, gflnm, gfpclc, tifcmf, gfcus, gfgrp, gfpcus, gfbrnm, gfctp1, gfcus1_sbb, gfmtb, gfaco, gfc101, gfc102, gfcpnc, gfdas, gfc201, gfc202, gfcrf, gfcus1, gfpcus1, clr_number, gfcun, ticustloc, gfcub, gfcuc, gfcud, gfcuz, mnt_in_bo)
VALUES
    (0, N'  ', N'   ', N'  ', N'  ', N'  ', N'  ', 1231218, N'  ', N'   ', N'Y', N'      ', N'      ', N'      ', N'        ', N'ASU     ', N'BPCH    ', N'        ', N'          ', N'          ', N'          ', N'1182146     ', N'ASEGURADORA DEL', N'0003                ', N'                    ', N'                    ', N'0190123626001       ', N'                    ', N'                                  ', N'ASEGURADORA DEL SUR CA             ', N'EC                                                          ', N'N', N'N', N'N', N'N', N'N');

INSERT INTO public.gfpf
    (date_dl, gfca2, gfclc, gfcnal, gfcnap, gfcnar, gfctp, gfdlm, gflnm, gfpclc, tifcmf, gfcus, gfgrp, gfpcus, gfbrnm, gfctp1, gfcus1_sbb, gfmtb, gfaco, gfc101, gfc102, gfcpnc, gfdas, gfc201, gfc202, gfcrf, gfcus1, gfpcus1, clr_number, gfcun, ticustloc, gfcub, gfcuc, gfcud, gfcuz, mnt_in_bo)
VALUES
    (0, N'  ', N'   ', N'EC', N'  ', N'  ', N'  ', 1231122, N'  ', N'   ', N'Y', N'      ', N'      ', N'      ', N'        ', N'SDC     ', N'BPCH    ', N'        ', N'          ', N'          ', N'          ', N'145896231022', N'DAVID REYES    ', N'0003                ', N'                    ', N'                    ', N'1722466420001       ', N'                    ', N'                                  ', N'DAVID REYES                        ', N'                                                            ', N'N', N'N', N'N', N'N', N'N');

INSERT INTO public.sx20lf
    (ADDRTYPE, SEQUENCE, SXCUS1_SBB, SXCUS1, EMAIL)
VALUES
    (1, 1, N'BPCH    ', N'1722466420001       ', N'dareyesp@pichincha.com');