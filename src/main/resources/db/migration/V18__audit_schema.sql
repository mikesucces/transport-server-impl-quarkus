-- V18 : audit des donnees.
-- Hibernate Envers alimente une table <table>_aud par table metier : a chaque creation, modification
-- ou suppression il y copie la ligne (valeurs completes) avec le numero de revision (rev) et le type
-- d'operation (revtype : 0 = creation, 1 = modification, 2 = suppression).
-- La table revinfo (id = numero de revision, timestamp en millisecondes) porte la date et l auteur (nom d'utilisateur du jeton) de chaque revision.

create sequence revinfo_seq start with 1 increment by 50;

create table revinfo (
    id        integer primary key,
    timestamp bigint,
    username  varchar(120)
);
create index idx_revinfo_timestamp on revinfo (timestamp);
create index idx_revinfo_username on revinfo (username);


create table access_codes_aud (
    id uuid,
    vehicle_id uuid,
    vehicle_number varchar(30),
    code_hash varchar(255),
    valid_from timestamptz,
    valid_until timestamptz,
    created_at timestamptz,
    rotation_id uuid,
    rev integer not null references revinfo (id),
    revtype smallint,
    primary key (id, rev)
);
create index idx_access_codes_aud_rev on access_codes_aud (rev);

create table attendances_aud (
    id uuid,
    rotation_id uuid,
    passenger_id uuid,
    access_code_id uuid,
    controller_id uuid,
    boarded_at timestamptz,
    rev integer not null references revinfo (id),
    revtype smallint,
    primary key (id, rev)
);
create index idx_attendances_aud_rev on attendances_aud (rev);

create table drivers_aud (
    id uuid,
    staff_profile_id uuid,
    full_name varchar(150),
    phone varchar(30),
    matricule varchar(30),
    license_number varchar(50),
    license_category varchar(10),
    license_expires_on date,
    remuneration_type varchar(20),
    monthly_salary numeric(12,2),
    trip_rate numeric(12,2),
    commission_rate numeric(5,2),
    status varchar(20),
    hired_on date,
    created_at timestamptz,
    updated_at timestamptz,
    rev integer not null references revinfo (id),
    revtype smallint,
    primary key (id, rev)
);
create index idx_drivers_aud_rev on drivers_aud (rev);

create table login_attempts_aud (
    id bigint,
    identifier varchar(150),
    passenger_id uuid,
    success boolean,
    ip_address varchar(45),
    attempted_at timestamptz,
    rev integer not null references revinfo (id),
    revtype smallint,
    primary key (id, rev)
);
create index idx_login_attempts_aud_rev on login_attempts_aud (rev);

create table maintenances_aud (
    id uuid,
    vehicle_id uuid,
    maintenance_type varchar(30),
    status varchar(20),
    scheduled_on date,
    performed_on date,
    mileage_km integer,
    next_due_km integer,
    next_due_on date,
    cost_amount numeric(12,2),
    garage varchar(150),
    description text,
    created_at timestamptz,
    updated_at timestamptz,
    rev integer not null references revinfo (id),
    revtype smallint,
    primary key (id, rev)
);
create index idx_maintenances_aud_rev on maintenances_aud (rev);

create table passengers_aud (
    id uuid,
    full_name varchar(150),
    phone varchar(30),
    status varchar(20),
    created_at timestamptz,
    updated_at timestamptz,
    rev integer not null references revinfo (id),
    revtype smallint,
    primary key (id, rev)
);
create index idx_passengers_aud_rev on passengers_aud (rev);

create table payment_accounts_aud (
    id uuid,
    passenger_id uuid,
    method varchar(20),
    label varchar(100),
    reference varchar(100),
    is_default boolean,
    active boolean,
    created_at timestamptz,
    updated_at timestamptz,
    rev integer not null references revinfo (id),
    revtype smallint,
    primary key (id, rev)
);
create index idx_payment_accounts_aud_rev on payment_accounts_aud (rev);

create table payments_aud (
    id uuid,
    subscription_id uuid,
    amount numeric(12,2),
    method varchar(20),
    collected_by uuid,
    paid_at timestamptz,
    payment_account_id uuid,
    rev integer not null references revinfo (id),
    revtype smallint,
    primary key (id, rev)
);
create index idx_payments_aud_rev on payments_aud (rev);

create table rental_payments_aud (
    id uuid,
    rental_id uuid,
    payment_account_id uuid,
    amount numeric(12,2),
    method varchar(20),
    collected_by uuid,
    paid_at timestamptz,
    rev integer not null references revinfo (id),
    revtype smallint,
    primary key (id, rev)
);
create index idx_rental_payments_aud_rev on rental_payments_aud (rev);

create table rentals_aud (
    id uuid,
    passenger_id uuid,
    vehicle_id uuid,
    status varchar(20),
    start_date date,
    end_date date,
    actual_return_date date,
    start_mileage_km integer,
    end_mileage_km integer,
    total_amount numeric(12,2),
    deposit_amount numeric(12,2),
    notes text,
    created_at timestamptz,
    updated_at timestamptz,
    rev integer not null references revinfo (id),
    revtype smallint,
    primary key (id, rev)
);
create index idx_rentals_aud_rev on rentals_aud (rev);

create table rotations_aud (
    id uuid,
    driver_id uuid,
    vehicle_id uuid,
    status varchar(20),
    scheduled_start timestamptz,
    scheduled_end timestamptz,
    started_at timestamptz,
    ended_at timestamptz,
    start_mileage_km integer,
    end_mileage_km integer,
    access_code_reference varchar(64),
    notes text,
    created_at timestamptz,
    updated_at timestamptz,
    route_id uuid,
    rev integer not null references revinfo (id),
    revtype smallint,
    primary key (id, rev)
);
create index idx_rotations_aud_rev on rotations_aud (rev);

create table route_schedules_aud (
    id uuid,
    route_id uuid,
    day_of_week varchar(10),
    departure_time time without time zone,
    created_at timestamptz,
    rev integer not null references revinfo (id),
    revtype smallint,
    primary key (id, rev)
);
create index idx_route_schedules_aud_rev on route_schedules_aud (rev);

create table routes_aud (
    id uuid,
    code varchar(20),
    name varchar(150),
    origin varchar(150),
    destination varchar(150),
    distance_km integer,
    status varchar(20),
    created_at timestamptz,
    updated_at timestamptz,
    rev integer not null references revinfo (id),
    revtype smallint,
    primary key (id, rev)
);
create index idx_routes_aud_rev on routes_aud (rev);

create table staff_profiles_aud (
    id uuid,
    keycloak_sub uuid,
    username varchar(100),
    full_name varchar(150),
    phone varchar(30),
    email varchar(150),
    staff_type varchar(20),
    matricule varchar(30),
    active boolean,
    first_seen_at timestamptz,
    last_login_at timestamptz,
    updated_at timestamptz,
    rev integer not null references revinfo (id),
    revtype smallint,
    primary key (id, rev)
);
create index idx_staff_profiles_aud_rev on staff_profiles_aud (rev);

create table subscriptions_aud (
    id uuid,
    passenger_id uuid,
    plan varchar(20),
    status varchar(20),
    starts_on date,
    ends_on date,
    created_at timestamptz,
    updated_at timestamptz,
    rev integer not null references revinfo (id),
    revtype smallint,
    primary key (id, rev)
);
create index idx_subscriptions_aud_rev on subscriptions_aud (rev);

create table vehicle_documents_aud (
    id uuid,
    vehicle_id uuid,
    doc_type varchar(30),
    reference varchar(80),
    issuer varchar(120),
    issued_on date,
    expires_on date,
    file_url varchar(500),
    created_at timestamptz,
    updated_at timestamptz,
    rev integer not null references revinfo (id),
    revtype smallint,
    primary key (id, rev)
);
create index idx_vehicle_documents_aud_rev on vehicle_documents_aud (rev);

create table vehicles_aud (
    id uuid,
    plate_number varchar(30),
    brand varchar(60),
    model varchar(60),
    year smallint,
    capacity smallint,
    mileage_km integer,
    status varchar(20),
    commissioned_at date,
    notes text,
    created_at timestamptz,
    updated_at timestamptz,
    rev integer not null references revinfo (id),
    revtype smallint,
    primary key (id, rev)
);
create index idx_vehicles_aud_rev on vehicles_aud (rev);
