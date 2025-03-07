# Use Node.js 18 for better performance
FROM node:18-alpine

# Set working directory inside container
WORKDIR /app

# Copy package.json and install dependencies
COPY package.json package-lock.json ./
RUN npm install --production

# Copy frontend source code
COPY . .

# Build Next.js app
RUN npm run build

# Expose port 3000 for Next.js
EXPOSE 3000

# Run Next.js in production mode
CMD ["npm", "run", "start"]
