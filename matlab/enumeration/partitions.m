
% N: the number of '1' bits in the bitstring
% -- the number of items to partition
function archs = Enum_partition_naive(N)

    % initialize empty variable array
    Architectures = {};
    if N == 0
        return;
    end

    % Architectures{N} - all partitioned architectures for N items to partition

    % if there is only one item to partition
    Architectures{1} = [1];

    % if there are only two items to partition
    Architectures{2} = [1 1;1 2];

    % if there are three or more items to partition
    % if N = 5
    % i = 3
    % i = 4
    % i = 5 ...
    for i = 3:N
        % Example: let i = 3
        n = 1;


        % a = 1:how many architectures are in Architectures{i-1}
        % Example: a = 1:2
        % a = 1
        % a = 2
        for a = 1:size(Architectures{i-1},1)

            % arch: contains all permuting architecture for 'a' elements
            arch = Architectures{i-1}(a,:);

            % find the max group number of all permuting archs for 'a' elements ???
            mx = max(arch) + 1;
            for j = 1:mx
                Architectures{i}(n,:) = cat(2,arch,j);
                n = n + 1;
            end
        end
    end

    archs = Architectures{N};
end