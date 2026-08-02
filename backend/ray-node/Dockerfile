FROM rayproject/ray:2.9.0-py310

# Switch to root for system package installation
USER root

# Install system dependencies required by SecretFlow/serving
# libgomp1: GNU OpenMP runtime, required by secretflow_serving_lib
RUN apt-get update && apt-get install -y --no-install-recommends \
    libgomp1 \
    && rm -rf /var/lib/apt/lists/*

# Create /data directory with proper permissions for ray user
RUN mkdir -p /data && chmod 777 /data

# Switch back to ray user for runtime
USER ray

# Why these specific pins (verified by test on 2026-07-31, do NOT change without re-validating):
#
#  - ray 2.2.0:
#      Base image ships Ray 2.9.0, but the running SecretFlow PSI flow
#      (cluster_config -> sf.init(ray_mode=False) + spu.psi) requires a
#      single consistent Ray version across head and worker. Tests showed
#      Ray 2.9.0 + 2.2.0 in the same cluster triggers
#      "Version mismatch: ... GCS has started but no raylets have
#      registered yet." Pinning both head and worker to 2.2.0 works.
#
#  - secretflow-lite 1.13.0b0 (NOT the full secretflow==1.14.0b0):
#      Full secretflow 1.14.0b0 pulls in secretflow-ray 2.2.0, which
#      overwrites ray/__init__.py with a 2.2.0 __version__ string while
#      the C++ binaries stay 2.9.0 -> version mismatch. secretflow-lite
#      provides the `sf` module with PRODUCTION/DEBUG modes only, which
#      is exactly what ray_mode=False uses. PRODUCTION is the mode the
#      PSI script uses, so the missing SIMULATION/RAY_PRODUCTION
#      strategies (which need the empty secretflow_fl package) are not
#      relevant here.
#
#  - spu / sf_heu: pulled transitively by secretflow-lite, pinned for
#    reproducibility so future secretflow-lite upgrades don't drift the
#    SPU runtime version we tested against.
RUN pip install --no-cache-dir \
    requests \
    numpy \
    pandas \
    scikit-learn \
    pyyaml \
    flask \
    pyarrow \
    scipy \
    && pip install --no-cache-dir \
        'ray==2.2.0' \
        'spu==0.9.4.dev20250618' \
        'sf_heu==0.6.0.dev20250514' \
        'secretflow-lite==1.13.0b0'

# Create work directory
WORKDIR /app

# Only the files start.sh / agent.py are actually used by ENTRYPOINT.
# Earlier versions also copied psi_service.py / psi_server.py / psi_runner.py
# / test_agent_ports.py / data/ but start.sh never references them - they
# are dead code left over from a previous iteration.
COPY start.sh /app/start.sh
COPY agent.py /app/agent.py

# Expose Ray ports and Agent HTTP port
EXPOSE 6379 8265 10001 8081

# Entry point
ENTRYPOINT ["bash", "/app/start.sh"]
