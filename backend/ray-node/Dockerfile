FROM rayproject/ray:2.9.0-py310

# Install privacy computing dependencies
RUN pip install --no-cache-dir \
    requests \
    numpy \
    pandas \
    scikit-learn \
    pyyaml \
    flask \
    pyarrow \
    scipy

# Install SecretFlow for privacy computing
RUN pip install --no-cache-dir secretflow==1.14.0b0

# Create work directory
WORKDIR /app

# Copy startup script and PSI service
COPY start.sh /app/start.sh
COPY psi_service.py /app/psi_service.py
COPY agent.py /app/agent.py
COPY data/ /data/

# Expose Ray ports and PSI service port
EXPOSE 6379 8265 10001 5000

# Entry point
ENTRYPOINT ["bash", "/app/start.sh"]
