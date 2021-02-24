export const getEnvVar = (envVar, fallback = "") => {
    return process.env[envVar] || fallback;
};